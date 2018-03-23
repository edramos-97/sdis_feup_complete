package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GetChunkRequest implements Callable<String>{
    private String fileId;
    private short chunkNo;

    public GetChunkRequest(String fileId, short chunkNo){
        this.fileId=fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public String call() {
        ProtocolMessage message;
        short i = 0;

        // BUILD MESSAGE
        message = new ProtocolMessage(ProtocolMessage.PossibleTypes.GETCHUNK);
        try {
            message.setFileId(fileId);
            message.setChunkNo(String.valueOf(chunkNo));
        } catch (Exception e) {
            //e.printStackTrace();
            return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage()+"\n";
        }

        //TODO - send getchunk command to MDB
        Peer.threadPool.schedule(new GetChunkVerification(0,message),400, TimeUnit.MILLISECONDS);
        return "";
    }
}
