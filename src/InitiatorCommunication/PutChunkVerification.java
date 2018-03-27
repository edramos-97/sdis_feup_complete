package InitiatorCommunication;

import Executables.Peer;
import Utilities.ProtocolMessage;

import java.util.concurrent.TimeUnit;

public class PutChunkVerification implements Runnable {
    private static short TIMEOUT = 1000;
    private static short MAX_TRIES = 5;

    private int tryNo;
    private ProtocolMessage message;

    PutChunkVerification(int tryNo,ProtocolMessage message){
        this.tryNo = tryNo;
        this.message = message;
    }

    @Override
    public void run() {
        if(tryNo>=MAX_TRIES){
            System.out.println("PutChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" finished unsuccessfully after "+MAX_TRIES+" tries.");
        }else{
            // TODO check replication Degree
            System.out.println("PutChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" failed try number "+tryNo+". Retrying...");
            // TODO resend message here



            tryNo++;
            Peer.threadPool.schedule(this,TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }
}
