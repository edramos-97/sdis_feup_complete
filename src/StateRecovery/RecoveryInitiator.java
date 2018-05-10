package StateRecovery;

import Executables.Peer;
import InitiatorCommunication.GetChunkRequest;
import InitiatorCommunication.PutChunkRequest;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RecoveryInitiator extends Thread {

    // FileId -> chunkNO
    // chunkNo >=0 && < 1000000   -> stored chunk
    // chunkNo >=1000000 && < 2000000  -> remove chunk
    // chunkNo == -1  -> backed up file
    // chunkNo == -2  -> deleted file backed up
    // chunkNo == -3  -> deleted stored file
    public static ConcurrentHashMap<String, List<Integer>> recoveryData = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<Integer>> volatileData = new ConcurrentHashMap<>();
    public static String fileID;
    public static int chunkNumber = 0;

    public RecoveryInitiator() {
        try {
            String localhostname = java.net.InetAddress.getLocalHost().getHostName() + Peer.peerID;
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(localhostname.getBytes());
            fileID = DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        File f = Paths.get(FileHandler.savePath + fileID).toFile();
        if(f.listFiles() != null) {
            chunkNumber = f.listFiles().length;
        } else {
            chunkNumber = 0;
        }
        //System.out.println(chunkNumber);


    }

    public static void dump(PrintStream os){
        //TODO - broadcast volatile data if not empty
        /*if(volatileData.isEmpty()) {
            return;
        }*/
        volatileData.forEach((k,v)->{
            os.print(k+"|");
            for (Integer i: v) {
                os.print(i + ",");
            }
            os.println();
        });
        volatileData.clear();

    }

    public static void addBackup(String fileName, long date){
        String fileNameAndDate = fileName + ":" + date;
        if(!volatileData.containsKey(fileNameAndDate)){
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-1);
            volatileData.put(fileNameAndDate,temp);
        }
    }

    public static void addDeleteBackup(String fileName, long date){
        String fileNameAndDate = fileName + ":" + date;
        if(volatileData.containsKey(fileNameAndDate)){
            volatileData.remove(fileNameAndDate);
        }else{
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-2);
            volatileData.put(fileNameAndDate,temp);
        }
        //TODO - add entry to volatile data
    }

    public static void addDeleteStored(String fileId){
        volatileData.remove(fileId);
        List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
        temp.listIterator().add(-3);
        volatileData.put(fileId,temp);
        //TODO - add entry to volatile data
    }

    public static void addStored(String fileId, int chunkNo){
        if(volatileData.containsKey(fileId)){
            List<Integer> temp = volatileData.get(fileId);
            if (temp!=null){
                temp.listIterator().add(chunkNo);
            }else{
                System.out.println("Couldn't add store log to volatile database");
            }
            // VolatileData.put(fileId, temp);
        }else {
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(chunkNo);
            volatileData.put(fileId, temp);
        }
        //TODO - add entry to volatile data
    }

    public static void addRemoved(String fileId,int chunkNo){
        //TODO - add entry to volatile data
        if(volatileData.containsKey(fileId)){
            List<Integer> temp = volatileData.get(fileId);
            if (temp!=null){
                temp.listIterator().add(chunkNo + 1000000);
            }else{
                System.out.println("Couldn't add removed log to volatile database");
            }
            // VolatileData.put(fileId, temp);
        }else {
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(chunkNo + 1000000);
            volatileData.put(fileId, temp);
        }

    }

    public static void restoreState(){
        recoveryData.forEach((k,v)->{
            if(!k.contains(":"))
                VolatileDatabase.restoreMemory.put(k,new Integer[]{-1,-1});
            for(Integer i : v) {
                if(i >= 0) {
                    Peer.threadPool.submit(new GetChunkRequest(k,i.shortValue(),"single","1.1" ));
                } else {
                    String[] nameDate = k.split(":",2);
                    MessageDigest digest;
                    try {
                        digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest((nameDate[0]+nameDate[1]).getBytes());
                        String hashedName = DatatypeConverter.printHexBinary(hash);
                        hashedName = hashedName.toLowerCase();
                        VolatileDatabase.restoreMemory.put(hashedName,new Integer[]{-1,-1});
                        Peer.threadPool.submit(new GetChunkRequest(hashedName,(short)0,nameDate[0],"1.1" ));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        do{
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(Peer.threadPool.getQueue().size()>1);
        //remove data from restore memory and change date on created files
        System.out.println("::::::::::::::::::::::::::::::::::::::::");
        recoveryData.forEach((k,v)->{
            File f;
            if (k.contains(":")){
                String[] nameDate = k.split(":");
                String filePath = FileHandler.restorePath+nameDate[0];
                f = Paths.get(filePath).toFile();
                if(f.setLastModified(Long.valueOf(nameDate[1]))){
                    try {
                        MessageDigest digest;
                        System.out.println("date successfully modified, backing up");
                        digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest((nameDate[0]+nameDate[1]).getBytes());
                        String hashedName = DatatypeConverter.printHexBinary(hash);
                        hashedName = hashedName.toLowerCase();
                        if(!VolatileDatabase.backed2fileID.containsKey(fileID) && hashedName.equals(""))
                            VolatileDatabase.backed2fileID.put(fileID, filePath);
                        Peer.threadPool.submit(new PutChunkRequest(filePath,(short)0,'1',1,"1.1"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("date modification failed");
                }
            }else {
                f = Paths.get(FileHandler.restorePath + k).toFile();
                if (f.renameTo(new File(FileHandler.savePath + k))){
                    System.out.println("renaming successful");
                }else{
                    System.out.println("renaming failed");
                }
                VolatileDatabase.restoreMemory.remove(k);
            }

        });
        recoveryData.clear();
        RecoveryInitiator.chunkNumber++;
        ProtocolMessage m = new ProtocolMessage(ProtocolMessage.PossibleTypes.GETLOGS);
        m.setFileId("0000000000000000000000000000000000000000000000000000000000000000");
        Dispatcher.sendControl(m.toCharArray());

    }

    @Override
    public void run() {
        ByteArrayOutputStream myStringArray = new ByteArrayOutputStream();
        PrintStream ps = null;
        try {
            ps = new PrintStream(myStringArray, true, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        dump(ps);
        byte[] messageBody = myStringArray.toByteArray();
        for (int i = 0; i*64000 < myStringArray.size(); i++) {
            ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.PUTLOGCHUNK);
            int size = 0;
            if(myStringArray.size() > 64000*(i + 1)) {
                size = 64000;
            } else {
                size = myStringArray.size()%64000;
            }
            message.setBody(Arrays.copyOfRange(messageBody, i*64000, size));
            try {
                message.setChunkNo(chunkNumber + "");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            message.setFileId(fileID);
            try {
                message.setReplicationDeg('1');
            } catch (Exception e) {
                e.printStackTrace();
            }
            Dispatcher.sendData(message.toCharArray());
            FileHandler.saveChunk(message, "backup");
            chunkNumber++;
        }
        Peer.threadPool.schedule(this, 1000, TimeUnit.MILLISECONDS);
    }
}
