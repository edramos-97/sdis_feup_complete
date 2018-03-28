package PackageRMI;

import Executables.Peer;
import InitiatorCommunication.DiskReclaimRequest;
import InitiatorCommunication.GetChunkRequest;
import InitiatorCommunication.PutChunkRequest;
import InitiatorCommunication.DeleteRequest;
import Utilities.FileHandler;

import java.rmi.RemoteException;

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
            for (int i = 0; i < Peer.MAX_CONCURRENCY; i++) {
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
    public boolean getChunk(String fileId, short chunkNo){
        System.out.println("Started GETCHUNK for fileID:\""+fileId+"\"");
        GetChunkRequest worker = new GetChunkRequest(fileId,chunkNo);
        Peer.threadPool.submit(worker);
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
    public boolean getState() throws RemoteException {
        return false;
    }


}
