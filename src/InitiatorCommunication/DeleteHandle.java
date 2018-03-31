package InitiatorCommunication;

import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DeleteHandle implements Runnable{

    ProtocolMessage message;

    public DeleteHandle(ProtocolMessage message){
        this.message = message;
    }
    @Override
    public void run() {
        FileHandler.removeFolder(new File(FileHandler.savePath + message.getFileId()));
        VolatileDatabase.get_database().remove(message.getFileId());
        message.setMsgType(ProtocolMessage.PossibleTypes.DELETECONF);

        byte[] message_bytes = message.toCharArray();
        System.out.println(new String(message_bytes));

        DatagramPacket packet;
        try {
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
                    InetAddress.getByName(MulticastChanel.multicast_control_address),
                    Integer.parseInt(MulticastChanel.multicast_control_port));
            MulticastChanel.multicast_control_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("Error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error in sending packet to multicast socket");
            e.printStackTrace();
        }
    }
}
