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
        FileHandler.saveChunk(this.message);
    }
}
