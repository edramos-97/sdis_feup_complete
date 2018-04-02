package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BackedupHandle implements Runnable {

    ProtocolMessage message;

    public BackedupHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run(){
        if (!VolatileDatabase.needDelete(message.getFileId(),message.getSenderId())){
            return;
        }

        message.setMsgType(ProtocolMessage.PossibleTypes.DELETE);
        try {
            message.setSenderId(String.valueOf(Peer.peerID));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        byte[] message_bytes = message.toCharArray();

        DatagramPacket packet;
        try {
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
                    InetAddress.getByName(MulticastChanel.multicast_control_address),
                    Integer.parseInt(MulticastChanel.multicast_control_port));
            MulticastChanel.multicast_control_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("BackedupHandle - Error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("BackedupHandle - Error in sending packet to multicast socket");
            e.printStackTrace();
        }
    }
}
