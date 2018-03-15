package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.IOException;
import java.util.Random;

public class StoreRequest implements Runnable{

    private String fileId;
    private short chunkNo;
    private char replicationDeg;
    byte[] data;
    private ProtocolMessage message;

    public StoreRequest(String fileId, short chunkNo, char replicationDeg, byte[] data) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.data = data;
    }

    @Override
    public void run() {
        this.message = new ProtocolMessage(ProtocolMessage.PossibleTypes.STORED);
        try {
            message.setFileId(this.fileId);
            message.setChunkNo(String.valueOf(chunkNo));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        int timeout = (new Random().nextInt())%400;
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO - hashMap.get(message.fileId+message.chunkNo).effReplicationDegree
        short effReplicationDegree = 5;
        if(effReplicationDegree>=this.replicationDeg){
            try {
                FileHandler.saveChunk(this.message.getFileId(),this.data,this.message.getChunkNo());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            //TODO - send stored message to MC
        }
    }
}
