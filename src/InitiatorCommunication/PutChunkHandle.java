package InitiatorCommunication;

import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class PutChunkHandle implements Runnable{

    private ProtocolMessage message;

    public PutChunkHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run() {
        if(message.getVersion().equals("1.1")){
            //TODO check replication deg
        }

        FileHandler.saveChunk(this.message);

        message.setMsgType(ProtocolMessage.PossibleTypes.STORED);

        MulticastSocket data_socket = MulticastChanel.multicast_control_socket;
        String message_bytes = new String(message.toCharArray()).trim();

        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(
                    message_bytes.getBytes(),
                    message_bytes.getBytes().length,
                    InetAddress.getByName(MulticastChanel.multicast_control_address),
                    Integer.parseInt(MulticastChanel.multicast_control_port));

            data_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("error in sending packet to multicast socket");
            e.printStackTrace();
        }
    }
}
