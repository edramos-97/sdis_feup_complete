package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.VolatileDatabase;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class PutChunkReclaim implements Runnable{


    private String filePath;
    private short chunkNo;
    private char replicationDeg;
    private String fileId;

    public PutChunkReclaim(String fileId, short chunkNo, char replicationDeg) throws Exception {
        this.filePath = FileHandler.getPath(fileId,chunkNo);

        if(new File(filePath).isDirectory()){
            throw new Exception("File path specified in PUTCHUNK request is a directory.\nFile Path: "+filePath);
        }
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        try {
            if(VolatileDatabase.removedChunk.indexOf(fileId+chunkNo)>=0){
                VolatileDatabase.removedChunk.remove(fileId+chunkNo);
                System.out.println("Filepath -> " + filePath);
                Peer.threadPool.submit(new PutChunkRequest(filePath,chunkNo,replicationDeg,0,fileId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
