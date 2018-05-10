package InitiatorCommunication;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

public class BackedupHandle implements Runnable {

    ProtocolMessage message;

    public BackedupHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run(){
        if (!VolatileDatabase.needDelete(message.getFileId(),message.getSenderId())){
            return;
        }

        message.setMsgType(ProtocolMessage.PossibleTypes.DELETE);
        try {
            message.setVersion("1.1");
            message.setSenderId(String.valueOf(Peer.peerID));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Dispatcher.sendControl(message.toCharArray());
    }
}
