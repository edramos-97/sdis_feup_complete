package InitiatorCommunication;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.ProtocolMessage;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class RecoverAskMaxRequest implements Runnable {

    private String fileIdentifier;

    public RecoverAskMaxRequest() {
        try {
            String localhostname = java.net.InetAddress.getLocalHost().getHostName() + Peer.peerID;
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(localhostname.getBytes());
            fileIdentifier = DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void run() {
        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.RECOVERASKMAX);

        message.setFileId(fileIdentifier);

        Dispatcher.sendControl(message.toCharArray());

        Peer.threadPool.schedule(new RecoverRequest(), 1000, TimeUnit.MILLISECONDS);
    }
}
