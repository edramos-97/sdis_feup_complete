package InitiatorCommunication;

import Executables.Peer;
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
        if(VolatileDatabase.get_database().get(message.getFileId())==null){
            return;
        }
        FileHandler.removeFolder(new File(FileHandler.savePath + message.getFileId()));
        VolatileDatabase.get_database().remove(message.getFileId());

        message.setMsgType(ProtocolMessage.PossibleTypes.DELETECONF);
        try {
            message.setSenderId(String.valueOf(Peer.peerID));
        } catch (Exception e) {
            System.out.println("DeleteHandle - Failed setting SenderID");
            //e.printStackTrace();
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
            System.out.println("DeleteHandle - Error in creating datagram packet.");
            //e.printStackTrace();
        } catch (IOException e) {
            System.out.println("DeleteHandle - Error in sending packet to multicast socket.");
            //e.printStackTrace();
        }
    }
}
