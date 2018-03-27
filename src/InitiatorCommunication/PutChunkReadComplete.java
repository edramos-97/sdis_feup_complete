package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.File_IO_Wrapper;
import MulticastThreads.MulticastChanelControl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;


public class PutChunkReadComplete implements CompletionHandler<Integer, File_IO_Wrapper>{
    @Override
    public void completed(Integer result, File_IO_Wrapper attachment) {
        //System.out.println(new String(attachment.getMessage().toCharArray()).trim());
        try {
            attachment.getFile().close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("READ COMPLETE");
        MulticastSocket data_socket = MulticastChanel.multicast_data_socket;
        String message_bytes = new String(attachment.getMessage().toCharArray()).trim();

        System.out.println(message_bytes.length());
        System.out.println(attachment.getMessage().body.toString());

        DatagramPacket packet = null;
        try {

            packet = new DatagramPacket(
                    message_bytes.getBytes(),
                    message_bytes.getBytes().length,
                    InetAddress.getByName(MulticastChanel.multicast_data_address),
                    Integer.parseInt(MulticastChanel.multicast_data_port));

            data_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("error in sending packet to multicast socket");
            e.printStackTrace();
        }

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
