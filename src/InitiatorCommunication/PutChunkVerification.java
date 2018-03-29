package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.File_IO_Wrapper;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.TimeUnit;

public class PutChunkVerification implements Runnable {
    private final static short TIMEOUT = 1000;
    private final static short MAX_TRIES = 5;
    private final Closeable openFile;

    private int tryNo;
    private ProtocolMessage message;

    PutChunkVerification(int tryNo, File_IO_Wrapper info){
        this.tryNo = tryNo;
        this.message = info.getMessage();
        this.openFile = info.getFile();
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
                //TODO Possibly stop if too many fails
                int nextChunkNo = Integer.parseInt(message.getChunkNo())+Peer.MAX_CONCURRENCY;
                if (nextChunkNo < message.getThreadNo()){
                    try {
                        Peer.threadPool.submit(new PutChunkRequest(message.getFile(),(short)nextChunkNo, Short.toString(message.getReplicationDeg()).charAt(0),message.getThreadNo()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("PUTCHUNK completed for fileId:\""+message.getFileId()+"\" Chunk:\""+message.getChunkNo()+"\"");
                return;
            }

            int delay = (int)Math.pow(2,tryNo)*TIMEOUT;
            tryNo++;
            Peer.threadPool.schedule(this,delay, TimeUnit.MILLISECONDS);
        }
    }
}