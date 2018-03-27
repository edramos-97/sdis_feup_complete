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
        if(message.getVersion().equals("1.1")){
            //TODO check replication deg
        }
        message.setMsgType(ProtocolMessage.PossibleTypes.STORED);
        //TODO send STORED message
        FileHandler.saveChunk(this.message);
    }
}
