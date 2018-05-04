package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.File_IO_Wrapper;
import Utilities.VolatileDatabase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class PutChunkReadComplete implements CompletionHandler<Integer, File_IO_Wrapper>{
    @Override
    public void completed(Integer result, File_IO_Wrapper attachment) {
        attachment.getMessage().body = Arrays.copyOfRange(attachment.getMessage().body,0,result);

        Dispatcher.sendData(attachment.getMessage().toCharArray());

        Peer.threadPool.schedule(new PutChunkVerification(1,attachment),1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void failed(Throwable exc, File_IO_Wrapper attachment) {
        System.out.println(exc.getMessage());
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            System.out.println("PutChunkReadComplete - Error: " + e.toString());
            //e.printStackTrace();
        }
    }
}
