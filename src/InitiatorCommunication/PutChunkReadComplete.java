package InitiatorCommunication;

import Utilities.ProtocolMessage;

import java.nio.channels.CompletionHandler;

public class PutChunkReadComplete implements CompletionHandler<Integer, ProtocolMessage>{
    @Override
    public void completed(Integer result, ProtocolMessage attachment) {
        System.out.println("Result was:" + result);
        System.out.println(new String(attachment.toCharArray()));
    }

    @Override
    public void failed(Throwable exc, ProtocolMessage attachment) {
        System.out.println(exc.getMessage());
    }
}
