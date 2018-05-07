package StateRecovery;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.UnknownHostException;
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
    private static ConcurrentHashMap<String, List<Integer>> recoveryData = new ConcurrentHashMap<>();
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
            this.chunkNumber = f.listFiles().length;
        } else {
            this.chunkNumber = 0;
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
        String fileNameAndDate = fileName + ";" + date;
        if(!volatileData.containsKey(fileNameAndDate)){
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-1);
            volatileData.put(fileNameAndDate,temp);
        }
    }

    public static void addDeleteBackup(String fileName, long date){
        String fileNameAndDate = fileName + ";" + date;
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
            System.out.println("here");
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
