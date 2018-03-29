package PackageRMI;

import Executables.Peer;
import InitiatorCommunication.DiskReclaimRequest;
import InitiatorCommunication.GetChunkRequest;
import InitiatorCommunication.PutChunkRequest;
import InitiatorCommunication.DeleteRequest;
import Utilities.*;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Control implements ControlInterface {

    public Control(){}

    @Override
    public void say_this(String s) {
        System.out.println(s);
    }

    @Override
    public boolean putChunk(String filePath, char replicationDeg) {
        int threadNo;

        try {
            threadNo = FileHandler.getChunkNo(filePath);
        } catch (Exception e) {
            System.out.println("PutChunk for fileID:\""+filePath+"\" finished unsuccessfully\n"+e.getMessage());
            return false;
        }
        System.out.println("Started PUTCHUNK for file:\""+filePath+"\"");

        //TODO save to backed2file
        //start working threads
        try {
            for (int i = 0; i < Peer.MAX_CONCURRENCY && i < threadNo; i++) {
                PutChunkRequest worker = new PutChunkRequest(filePath, (short)i, replicationDeg, threadNo);
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
    public boolean getChunk(String filePath){
        File file = new File(filePath);
        if (!file.exists()){
            System.out.println("RESTORE terminated, file doesn't exist: "+filePath);
            return false;
        }
        String fileId = FileHandler.getFileId(file);
        if (fileId == null){
            System.out.println("RESTORE terminated, file is a directory: "+filePath);
            return false;
        }
        System.out.println("Started GETCHUNK for fileID:\""+fileId+"\"");
        //initialize getchunk request in data base
        VolatileDatabase.restoreMemory.put(fileId,new Integer[]{-1,-1});
        Peer.threadPool.submit(new GetChunkRequest(fileId,(short)0));
        return true;
    }

    @Override
    public boolean delete(String path) throws RemoteException {
        Peer.threadPool.submit(new DeleteRequest(path));
        return true;
    }

    @Override
    public boolean reclaim(long desiredAllocation) throws RemoteException {
        Peer.threadPool.submit(new DiskReclaimRequest(desiredAllocation));
        return false;
    }

    @Override
    public StateOfPeer getState() throws RemoteException {

        StateOfPeer sop = new StateOfPeer(Peer.peerID);

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
                sf.addChunk(cf);
            }

            // Add Stored File to StateOfPeer
            sop.addFile(sf);
        }

        // TODO create file to then return it


        return sop;
    }
}
