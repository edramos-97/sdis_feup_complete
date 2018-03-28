package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.File;
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
        if(!FileHandler.hasChunk(fileId,chunkNo)){
            return;
        }else{
            System.out.println("CHECKING FOR REPLICATION DEGREE");
        }
        //TODO get file desired repDeg and send PUTCHUNK
        short repDeg = 1;
        int delay = new Random().nextInt(400);
        try {
            //Peer.threadPool.schedule(new PutChunkRequest(path,chunkNo,Short.toString(repDeg).toCharArray()[1]),delay, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
