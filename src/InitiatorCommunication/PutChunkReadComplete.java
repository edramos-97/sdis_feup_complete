package InitiatorCommunication;

import Executables.Peer;
import Utilities.File_IO_Wrapper;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

public class PutChunkReadComplete implements CompletionHandler<Integer, File_IO_Wrapper>{
    @Override
    public void completed(Integer result, File_IO_Wrapper attachment) {
        System.out.println(new String(attachment.getMessage().toCharArray()).trim());
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO send PUTCHUNK message
        Peer.threadPool.schedule(new PutChunkVerification(1,attachment.getMessage()),1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void failed(Throwable exc, File_IO_Wrapper attachment) {
        System.out.println(exc.getMessage());
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
