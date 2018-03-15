package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import java.io.File;

public class GetChunkRequest extends Thread{
    private String fileId;
    private short chunkNo;
    private ProtocolMessage message;
    private static short TIMEOUT = 1000;
    private static short MAX_TRIES = 5;

    public GetChunkRequest(String fileId, short chunkNo){
        this.fileId=fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        super.run();
        short i = 0;
        this.message = new ProtocolMessage(ProtocolMessage.PossibleTypes.GETCHUNK);
        try {
            message.setFileId(this.fileId);
            message.setChunkNo(String.valueOf(chunkNo));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        while (i<MAX_TRIES){
            //TODO - send getchunk command to MDB
            try {
                Thread.sleep(TIMEOUT);
                if (FileHandler.hasChunk(this.fileId,this.chunkNo))
                    return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
