package InitiatorCommunication;

import Utilities.File_IO_Wrapper;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

public class PutChunkHandleComplete implements CompletionHandler<Integer, File_IO_Wrapper> {
    @Override
    public void completed(Integer result, File_IO_Wrapper attachment) {
        System.out.println("STORED file with Id:\""+attachment.getMessage().getFileId()+"\" and chunkNo:\""+attachment.getMessage().getChunkNo()+"\"");
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            System.out.println("PutChunkHandleComplete - Error: " + e.toString());
            //e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, File_IO_Wrapper attachment) {
        System.out.println(exc.getMessage());
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            System.out.println("PutChunkHandleComplete - Error: " + e.toString());
            //e.printStackTrace();
        }
    }
}
