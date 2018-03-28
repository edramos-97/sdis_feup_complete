package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class PutChunkVerification implements Runnable {
    private static short TIMEOUT = 1000;
    private static short MAX_TRIES = 5;

    private int tryNo;
    private ProtocolMessage message;

    PutChunkVerification(int tryNo,ProtocolMessage message){
        this.tryNo = tryNo;
        this.message = message;
    }

    @Override
    public void run() {
        if(tryNo>=MAX_TRIES){
            System.out.println("PutChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" finished unsuccessfully after "+MAX_TRIES+" tries.");
        }else{
            // Checking replication degree of file

            short current_rep_degree = VolatileDatabase.get_rep_degree(message.getFileId(), Short.valueOf(message.getChunkNo()));
            if (current_rep_degree < message.getReplicationDeg()){

                // Resending message to everyone, hoping someone new accepts the chunk.

                System.out.println("PutChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" failed try number "+tryNo+". Retrying...");

                MulticastSocket data_socket = MulticastChanel.multicast_data_socket;
                byte[] message_bytes = message.toCharArray();

                DatagramPacket packet;
                try {
                    packet = new DatagramPacket(
                            message_bytes,
                            message_bytes.length,
                            InetAddress.getByName(MulticastChanel.multicast_data_address),
                            Integer.parseInt(MulticastChanel.multicast_data_port));
                    data_socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }else{
                return;
            }

            tryNo++;
            Peer.threadPool.schedule(this,TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }
}
