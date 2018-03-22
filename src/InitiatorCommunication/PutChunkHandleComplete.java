package InitiatorCommunication;

import Executables.Peer;
import Utilities.File_IO_Wrapper;
import Utilities.ProtocolMessage;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

public class PutChunkHandleComplete implements CompletionHandler<Integer, File_IO_Wrapper> {
    @Override
    public void completed(Integer result, File_IO_Wrapper attachment) {
        System.out.println("STORED file with Id:\""+attachment.getMessage().getFileId()+"\" and chunkNo:\""+attachment.getMessage().getChunkNo());
        attachment.getMessage().setMsgType(ProtocolMessage.PossibleTypes.STORED);
        //TODO send message
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
