package PackageRMI;

import Executables.Peer;
import InitiatorCommunication.GetChunkRequest;
import InitiatorCommunication.PutChunkRequest;
import Utilities.FileHandler;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Control implements ControlInterface {

    public Control(){}

    @Override
    public void say_this(String s) {
        System.out.println(s);
    }

    @Override
    public boolean putChunk(String filePath, char replicationDeg) {
        int threadNo;

        //check if the file
        try {
            threadNo = FileHandler.getChunkNo(filePath);
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("PutChunk for fileID:\""+filePath+"\" finished unsuccessfully\n"+e.getMessage());
            return false;
        }
        System.out.println("Started PUTCHUNK for file:\""+filePath+"\"");

        //TODO save to backed2file
        //start working threads
        try {
            for (int i = 0; i < threadNo; i++) {
                PutChunkRequest worker = new PutChunkRequest(filePath, (short)i, replicationDeg);
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
        Future finalized = Peer.threadPool.submit(worker);
        new Thread(() -> {
            try {
                System.out.println(finalized.get(GetChunkRequest.TIMEOUT*GetChunkRequest.MAX_TRIES+100, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully");
                System.out.println(e.getMessage());
            }
        }).start();
        return true;
    }
}
