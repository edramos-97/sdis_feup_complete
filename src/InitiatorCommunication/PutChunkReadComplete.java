package InitiatorCommunication;

import Executables.Peer;
import Utilities.ProtocolMessage;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

public class PutChunkReadComplete implements CompletionHandler<Integer, ProtocolMessage>{
    @Override
    public void completed(Integer result, ProtocolMessage attachment) {
        System.out.println(new String(attachment.toCharArray()).trim());
        //TODO send message
        Peer.threadPool.schedule(new PutChunkVerification(1,attachment),1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void failed(Throwable exc, ProtocolMessage attachment) {
        System.out.println(exc.getMessage());
    }
}
