package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.FileInfo;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DiskReclaimHandle implements Runnable{

    private String fileId;
    private short chunkNo;
    private int peerR;

    public DiskReclaimHandle(String fileId, short chunkNo, int peerRemoved){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.peerR = peerRemoved;
    }


    @Override
    public void run() {
        //System.out.println("CHECKING FOR REPLICATION DEGREE");
        FileInfo info = VolatileDatabase.getInfo(fileId,chunkNo);
        if (info == null){
            System.out.println("RECLAIM could not find an entry for the file requested");
            return;
        }
        VolatileDatabase.chunkDeleted(fileId,chunkNo,peerR);

        if(FileHandler.hasChunk(fileId,chunkNo)){

            if (info.getRequiredRepDeg()>info.getRepDeg()){
                int delay = new Random().nextInt(400);
                try {
                    VolatileDatabase.removedChunk.add(fileId+chunkNo);
                    System.out.println(fileId);
                    Peer.threadPool.schedule(new PutChunkReclaim(fileId,chunkNo,Short.toString(info.getRequiredRepDeg()).charAt(0)),delay, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
