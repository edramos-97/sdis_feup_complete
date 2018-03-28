package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class PutChunkHandle extends Thread {

    private ProtocolMessage message;

    public PutChunkHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run() {
        if(message.getVersion().equals("1.1")){
            System.out.println("ENHANCEMENT BACKUP");

            // TODO-Enhancement Backup Checking if already have enough replication degree then its meaningless to store any more

            short current_rep_degree = VolatileDatabase.get_rep_degree(message.getFileId(), Short.valueOf(message.getChunkNo()));

            System.out.println("curr: " + current_rep_degree);
            System.out.println("thrs: " + message.getReplicationDeg());

            if (current_rep_degree >= message.getReplicationDeg()){
                return;
            }
        }

        VolatileDatabase.add_chunk(message.getFileId(),Short.valueOf(message.getChunkNo()),message.getReplicationDeg());


        message.setMsgType(ProtocolMessage.PossibleTypes.STORED);
        try {
            message.setSenderId(String.valueOf(Peer.peerID));
        } catch (Exception e) {
            e.printStackTrace();
        }

        MulticastSocket data_socket = MulticastChanel.multicast_control_socket;
        byte[] message_bytes = message.toCharArray();

        System.out.println("STORED TO SEND: "+ Arrays.toString(message_bytes));
        System.out.println("STORED TO SEND: "+ new String(message_bytes));

        DatagramPacket packet;
        try {
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
                    InetAddress.getByName(MulticastChanel.multicast_control_address),
                    Integer.parseInt(MulticastChanel.multicast_control_port));
            data_socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("Error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error in sending packet to multicast socket");
            e.printStackTrace();
        }

        FileHandler.saveChunk(this.message);

    }
}
