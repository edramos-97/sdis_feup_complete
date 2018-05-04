package InitiatorCommunication;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class PutChunkReclaim implements Runnable{


    private String filePath;
    private short chunkNo;
    private char replicationDeg;
    private String fileId;

    PutChunkReclaim(String fileId, short chunkNo, char replicationDeg) throws Exception {
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
                Peer.threadPool.submit(new PutChunkRequest(filePath,chunkNo,replicationDeg,0,fileId,"1.0"));
                Peer.threadPool.schedule(()->{
                    ProtocolMessage message;
                    message = new ProtocolMessage(ProtocolMessage.PossibleTypes.STORED);
                    message.setFileId(fileId);
                    try {
                        message.setChunkNo(String.valueOf(chunkNo));
                    } catch (Exception e) {
                        System.out.println("PutChunkReclaim - Error setting ChunkNumber.");
                        //e.printStackTrace();
                    }
                    Dispatcher.sendControl(message.toCharArray());
                },400,TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            System.out.println("PutChunkReclaim - Error: " + e.toString());
            //e.printStackTrace();
        }
    }
}
