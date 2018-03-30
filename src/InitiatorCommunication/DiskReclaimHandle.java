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

    public DiskReclaimHandle(String fileId, short chunkNo){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }


    @Override
    public void run() {
        if(FileHandler.hasChunk(fileId,chunkNo)){
            System.out.println("CHECKING FOR REPLICATION DEGREE");
            FileInfo info = VolatileDatabase.getInfo(fileId,chunkNo);
            if (info == null){
                System.out.println("RECLAIM could not find an entry for the file requested");
                return;
            }
            if (info.getRequiredRepDeg()>info.getRepDeg()){
                int delay = new Random().nextInt(400);
                try {
                    Peer.threadPool.schedule(new PutChunkRequest(FileHandler.getPath(fileId,chunkNo),chunkNo,Short.toString(info.getRequiredRepDeg()).charAt(0),0),delay, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
