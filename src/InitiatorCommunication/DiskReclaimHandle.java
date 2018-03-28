package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DiskReclaimHandle implements Runnable{

    private String fileId;
    private short chunkNo;

    public DiskReclaimHandle(String fileId, short chunkNo){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }


    @Override
    public void run() {
        String path = FileHandler.getPath(fileId,chunkNo);
        //TODO get file desired repDeg
        short repDeg = 1;
        int delay = new Random().nextInt(400);
        try {
            //Peer.threadPool.schedule(new PutChunkRequest(path,chunkNo,Short.toString(repDeg).toCharArray()[1]),delay, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
