package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class GetChunkRequest implements Callable<String>{
    private String fileId;
    private short chunkNo;
    private ProtocolMessage message;
    public static short TIMEOUT = 1000;
    public static short MAX_TRIES = 5;

    public GetChunkRequest(String fileId, short chunkNo){
        this.fileId=fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public String call() {
        short i = 0;
        this.message = new ProtocolMessage(ProtocolMessage.PossibleTypes.GETCHUNK);
        try {
            message.setFileId(fileId);
            message.setChunkNo(String.valueOf(chunkNo));
        } catch (Exception e) {
            //e.printStackTrace();
            return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage()+"\n";
        }
        while (i<MAX_TRIES){
            //TODO - send getchunk command to MDB
            try {
                Thread.sleep(TIMEOUT);
                if (FileHandler.hasChunk(this.fileId,this.chunkNo))
                    return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished successfully\n";
                else{
                    i++;
                    if (i!=MAX_TRIES)
                        System.out.println("GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" failed to get an answer on try No:"+i+", retrying...");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n";
            }
        }
        return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully\nDidn't receive a valid answer after "+i+" tries\n";
    }
}
