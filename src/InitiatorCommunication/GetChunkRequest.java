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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GetChunkRequest implements Callable<String>{
    private String fileId;
    private short chunkNo;
    private String fileName;
    String version;

    public GetChunkRequest(String fileId, short chunkNo,String fileName,String version){
        this.fileId=fileId;
        this.chunkNo = chunkNo;
        this.fileName = fileName;
        this.version = version;
    }

    @Override
    public String call() {
        ProtocolMessage message;

        // BUILD MESSAGE
        message = new ProtocolMessage(ProtocolMessage.PossibleTypes.GETCHUNK);
        try {
            message.setFileId(fileId);
            message.setVersion(version);
            message.setChunkNo(String.valueOf(chunkNo));
        } catch (Exception e) {
            //e.printStackTrace();
            return "GetChunk for fileID:\""+fileId+"\" chunkNo:"+chunkNo+" finished unsuccessfully\n"+e.getMessage()+"\n";
        }

        //send Getchunk
        MulticastSocket data_socket = MulticastChanel.multicast_control_socket;
        byte[] message_bytes = message.toCharArray();

        DatagramPacket packet;
        try {
            packet = new DatagramPacket(
                    message_bytes,
                    message_bytes.length,
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

        Peer.threadPool.schedule(new GetChunkVerification(0,message,fileName),450, TimeUnit.MILLISECONDS);
        return "";
    }
}
