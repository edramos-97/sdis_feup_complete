package InitiatorCommunication;

import Executables.Peer;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.util.concurrent.TimeUnit;

public class GetChunkVerification implements Runnable {

    private static short TIMEOUT = 1000;
    private static short MAX_TRIES = 5;
    private ProtocolMessage message;
    private int tryNo;

    GetChunkVerification(int i, ProtocolMessage message) {
        this.tryNo = i;
        this.message = message;
        //TODO create tcp socket
    }

    @Override
    public void run() {
        //TODO call tcp socket accept to wait for connection
        //TODO possibly use socket.setSoTimeout

        //TODO do some stuff to start data connection anf get data
        //TODO save data
        if (FileHandler.hasChunk(message.getFileId(), Short.valueOf(message.getChunkNo()))){
            System.out.println("GetChunk for fileID:\"" + message.getFileId() + "\" chunkNo:" + message.getChunkNo() + " finished successfully");
        }
        else {
            tryNo++;
            System.out.println("GetChunk for fileID:\"" + message.getFileId() + "\" chunkNo:" + message.getChunkNo() + " failed to get an answer on try No:" + tryNo + ", retrying...");
            if (tryNo >= MAX_TRIES){
                System.out.println("GetChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" finished unsuccessfully\nDidn't receive a connection request after "+MAX_TRIES+" tries");
                return;
            }
            Peer.threadPool.schedule(this,400, TimeUnit.MILLISECONDS);
        }
    }
}
