package InitiatorCommunication;

import Executables.Peer;
import StateRecovery.RecoveryInitiator;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;

public class DeleteHandle implements Runnable{

    ProtocolMessage message;

    public DeleteHandle(ProtocolMessage message){
        this.message = message;
    }
    @Override
    public void run() {
        if(VolatileDatabase.get_database().get(message.getFileId())==null){
            return;
        }
        FileHandler.removeFolder(new File(FileHandler.savePath + message.getFileId()));
        VolatileDatabase.get_database().remove(message.getFileId());

        if (message.getVersion().equals("1.1")) {
            message.setMsgType(ProtocolMessage.PossibleTypes.DELETECONF);
            try {
                message.setSenderId(String.valueOf(Peer.peerID));
            } catch (Exception e) {
                System.out.println("DeleteHandle - Failed setting SenderID");
                //e.printStackTrace();
                return;
            }
            Dispatcher.sendControl(message.toCharArray());
        }
        RecoveryInitiator.addDeleteStored(message.getFileId());
    }
}
