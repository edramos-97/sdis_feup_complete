package InitiatorCommunication;

import Executables.Peer;
import StateRecovery.RecoveryInitiator;
import Utilities.Dispatcher;
import Utilities.ProtocolMessage;

import java.util.concurrent.TimeUnit;

public class RecoverAskMaxRequest implements Runnable {

    public RecoverAskMaxRequest() {

    }

    @Override
    public void run() {
        RecoveryInitiator.active = true;
        RecoveryInitiator.chunkNumber = 0;

        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.RECOVERASKMAX);

        message.setFileId(RecoveryInitiator.fileID);

        Dispatcher.sendControl(message.toCharArray());

        Peer.threadPool.schedule(new RecoverRequest(), 1000, TimeUnit.MILLISECONDS);
    }
}
