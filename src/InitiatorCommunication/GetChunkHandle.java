package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;
import org.omg.CORBA.PERSIST_STORE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class GetChunkHandle extends Thread {

    ProtocolMessage message;

    public GetChunkHandle(ProtocolMessage message){
        this.message = message;
    }

    @Override
    public void run(){
        //check if a chunk has been received
        if (VolatileDatabase.getChunkMemory.indexOf(message.getFileId()+message.getChunkNo())>=0){
            System.out.println("SENDING CHUNK BACK");
            //send message and remove table entry for fileID+chunkNo
            try {
                message.setBody(FileHandler.getChunk(message.getFileId(),Short.valueOf(message.getChunkNo())));
                message.setMsgType(ProtocolMessage.PossibleTypes.CHUNK);
                message.setSenderId(Integer.toString(Peer.peerID));
            } catch (Exception e) {
                e.printStackTrace();
            }

            MulticastSocket data_socket = MulticastChanel.multicast_recover_socket;
            byte[] message_bytes = message.toCharArray();

            DatagramPacket packet;
            try {
                packet = new DatagramPacket(
                        message_bytes,
                        message_bytes.length,
                        InetAddress.getByName(MulticastChanel.multicast_recover_address),
                        Integer.parseInt(MulticastChanel.multicast_recover_port));
                data_socket.send(packet);
            } catch (UnknownHostException e) {
                System.out.println("error in creating datagram packet");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("error in sending packet to multicast socket");
                e.printStackTrace();
            }
            VolatileDatabase.getChunkMemory.remove(message.getFileId()+message.getChunkNo());
        }else{
            System.out.println("CHUNK ALREADY RECEIVED");
        }
    }
}
