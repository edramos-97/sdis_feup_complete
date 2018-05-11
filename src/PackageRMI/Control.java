package PackageRMI;

import Executables.Peer;
import InitiatorCommunication.*;
import StateRecovery.RecoveryInitiator;
import Utilities.FileHandler;
import Utilities.VolatileDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Control implements ControlInterface {

    public Control(){}

    @Override
    public void say_this(String s) {
        System.out.println(s);
    }

    @Override
    public boolean putChunk(String filePath, char replicationDeg, boolean enhanced) {

        File file = Paths.get(filePath).toFile();

        String fileName = file.getName();
        long date = file.lastModified();

        int threadNo;

        try {
            threadNo = FileHandler.getChunkNo(filePath);
        } catch (Exception e) {
            System.out.println("PutChunk for fileID:\""+filePath+"\" finished unsuccessfully\n"+e.getMessage());
            return false;
        }
        System.out.println("Started PUTCHUNK for file:\""+filePath+"\"");

        RecoveryInitiator.addBackup(fileName, date,Integer.valueOf(""+replicationDeg));

        //start working threads
        String version = enhanced?"1.0":"1.1";
        try {
            for (int i = 0; i < Peer.MAX_CONCURRENCY && i < threadNo; i++) {
                PutChunkRequest worker = new PutChunkRequest(filePath, (short)i, replicationDeg, threadNo,version);
                Peer.threadPool.submit(worker);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("PutChunk for fileID:\""+filePath+"\" finished unsuccessfully");
            System.out.println(e.getMessage());
        }

        return true;
    }

    @Override
    public boolean getChunk(String fileName, boolean enhanced){
        /*File file = new File(fileNath);
        if (!file.exists()){
            System.out.println("RESTORE terminated, file doesn't exist: "+fileName);
            return false;
        }*/

        //String fileId = FileHandler.getFileId(file);
        ArrayList<String> fileId = new ArrayList<>();
        VolatileDatabase.backed2fileID.forEach((k,v)->{
            String[] temp;
            if(File.separator == "\\") {
                System.out.println("ups this is not tested...");
                temp = v.split("\\\\");
            } else{
                temp = v.split("/");
            }
            System.out.println("temp:"+ Arrays.toString(temp));
            if(fileName.equals(temp[temp.length - 1])){
                System.out.println("here added");
                fileId.add(k);
            }
        });
        if(fileId.isEmpty()){
            System.out.println("RESTORE terminated, couldn't find reference for that file name:"+fileName);
            return false;
        }
        /*if (fileId == null){
            System.out.println("RESTORE terminated, file is a directory: "+fileName);
            return false;
        }*/

        System.out.println("Started GETCHUNK for fileID:\""+fileId+"\"");

        String version = enhanced?"1.0":"1.1";

        /*if(version.equals("1.1")){
            Peer.threadPool.submit(new RestoreEnhancement());
        }*/

        //initialize getchunk request in data base
        VolatileDatabase.restoreMemory.put(fileId.get(fileId.size()-1),new Integer[]{-1,-1});

        Peer.threadPool.submit(new GetChunkRequest(fileId.get(fileId.size()-1),(short)0,fileName,version));
        return true;
    }

    @Override
    public boolean delete(String path, boolean enhanced) {
        String version = enhanced?"1.0":"1.1";
        Peer.threadPool.submit(new DeleteRequest(path, version));
        return true;
    }

    @Override
    public boolean reclaim(long desiredAllocation) {
        Peer.threadPool.submit(new DiskReclaimRequest(desiredAllocation));
        return false;
    }

    @Override
    public String getState() {

        ByteArrayOutputStream myStringArray = new ByteArrayOutputStream();
        PrintStream ps = null;
        try {
            ps = new PrintStream(myStringArray, true, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (ps == null){
            return "ERROR IN PRINT STREAM INITIALIZATION";
        }

        ps.println("This was the state of my internals...");
        ps.println("RESTORE INITIATED IS EMPTY: "+VolatileDatabase.restoreMemory.isEmpty());
        ps.println("GETCHUNK WAITING ANSWER IS EMPTY: "+VolatileDatabase.getChunkMemory.isEmpty());

        ps.println("::::::::::::::: PEER INFO :::::::::::::::");
        ps.println("MAX ALLOCATED SPACE AVAILABLE: "+FileHandler.getFreeSpace()/1000.0 +"kB");
        ps.println("MAX AVAILABLE STORAGE SPACE: "+FileHandler.getAllocatedSpace()/1000.0+"kB");
        ps.println("USED STORAGE SPACE: "+FileHandler.getDiskUsage()/1000.0+"kB\n");
        VolatileDatabase.print(ps);

        ps.close();
        return new String(myStringArray.toByteArray(), StandardCharsets.UTF_8);

        /*StateOfPeer sop = new StateOfPeer(Peer.peerID);

        ConcurrentHashMap<String, List<FileInfo>> database = VolatileDatabase.get_database();
        ConcurrentHashMap<String, String> backed_files = VolatileDatabase.get_backed_up();

        for (Map.Entry<String, List<FileInfo>> pair : database.entrySet()) {
            // Declaring Stored File
            StoredFile sf;
            String fileID = pair.getKey();
            String pathname="";
            int desiredReplicationDegree = 0;

            // Searching for pathname
            for (Map.Entry<String, String> pair2  : backed_files.entrySet()) {
                if(pair2.getValue().equals(fileID)){
                    pathname = pair2.getKey();
                    break;
                }
            }

            // Searching for desired replication degree
            desiredReplicationDegree = pair.getValue().get(0).getRequiredRepDeg();

            // Creating Stored File
            sf = new StoredFile(fileID, pathname, desiredReplicationDegree);

            // Getting all the Chunk's Info
            for(FileInfo fi : pair.getValue()){
                ChunkFile cf = new ChunkFile(fi.getChunkNo(), fi.getRepDeg(), 0);

                for(Integer i : fi.stored_peers){
                    cf.addPeer(i);
                }

                sf.addChunk(cf);
            }

            // Add Stored File to StateOfPeer
            sop.addFile(sf);
        }

        return sop;*/
    }

    @Override
    public void dumpLog() {
        //RecoveryInitiator.dump();
    }

    @Override
    public void recover() {
        Peer.threadPool.submit(new RecoverAskMaxRequest());
    }
}
