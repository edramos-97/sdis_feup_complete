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
import java.util.concurrent.TimeUnit;

public class PutChunkHandle extends Thread {

    private ProtocolMessage message;

    public PutChunkHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run() {
        if(message.getVersion().equals("1.1")){
            System.out.println("RUNNING ENHANCED BACKUP");
            //TODO-Enhancement Backup Checking if already have enough replication degree then its meaningless to store any more
            short current_rep_degree = VolatileDatabase.get_rep_degree(message.getFileId(), Short.valueOf(message.getChunkNo()));

            if (current_rep_degree >= message.getReplicationDeg()){
                Peer.threadPool.schedule(()-> VolatileDatabase.deleteChunkEntry(message.getFileId(), Short.valueOf(message.getChunkNo())),1000, TimeUnit.MILLISECONDS);
                return;
            }
        }

        message.setMsgType(ProtocolMessage.PossibleTypes.STORED);
        try {
            message.setSenderId(String.valueOf(Peer.peerID));
        } catch (Exception e) {
            e.printStackTrace();
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
            FileHandler.saveChunk(this.message,"backup");

            VolatileDatabase.add_chunk_stored(message.getFileId(), Short.valueOf(message.getChunkNo()), Peer.peerID);

        } catch (UnknownHostException e) {
            System.out.println("Error in creating datagram packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error in sending packet to multicast socket");
            e.printStackTrace();
        }


    }
}
