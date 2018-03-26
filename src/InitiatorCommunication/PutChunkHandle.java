package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.IOException;

public class PutChunkHandle implements Runnable{

    private ProtocolMessage message;

    PutChunkHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run() {
        //TODO check replication deg
        //TODO send stored message
        FileHandler.saveChunk(this.message);
    }
}
