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
        MulticastSocket data_socket = MulticastChanel.multicast_data_socket;
        attachment.getMessage().body = Arrays.copyOfRange(attachment.getMessage().body,0,result);
        byte[] message_bytes = attachment.getMessage().toCharArray();

        DatagramPacket packet;
        try {

            VolatileDatabase.add_chunk_putchunk(attachment.getMessage().getFileId(), Short.valueOf(attachment.getMessage().getChunkNo()), attachment.getMessage().getReplicationDeg(), -1);

            attachment.getFile().close();
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
                    InetAddress.getByName(MulticastChanel.multicast_data_address),
                    Integer.parseInt(MulticastChanel.multicast_data_port));
            data_socket.send(packet);

            //VolatileDatabase.add_chunk_putchunk(attachment.getMessage().getFileId(), Short.valueOf(attachment.getMessage().getChunkNo()), attachment.getMessage().getReplicationDeg(), -1);

        } catch (UnknownHostException e) {
            System.out.println("error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("error in sending packet to multicast socket");
            e.printStackTrace();
        }

        Peer.threadPool.schedule(new PutChunkVerification(1,attachment),1000, TimeUnit.MILLISECONDS);
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
