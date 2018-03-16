package PackageRMI;

import Executables.Peer;
import InitiatorCommunication.PutChunkRequest;
import Utilities.FileHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Control implements ControlInterface {

    public Control(){

    }

    @Override
    public boolean putChunk(String filePath, char replicationDeg) {
        int threadNo = 0;
        try {
            threadNo = FileHandler.getChunkNo(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Future[] threads = new Future[threadNo];
        System.out.println("Started putchunk for file:\""+filePath+"\"");
        try {
            for (int i = 0; i < threadNo; i++) {
                PutChunkRequest worker = new PutChunkRequest(filePath, (short)i, replicationDeg);
                threads[i]=Peer.threadPool.submit(worker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Future worker: threads) {
            new Thread(() -> {
                try {
                    System.out.println(worker.get(5500, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Putchunk for fileID:b2188d73694e425cdf619b47b96c84728e87a30cb274441de843666c0106c3d3 chunkNo:0 finished unsuccessfully");
                }
            }).start();
        }
        return true;
    }
}
