package InitiatorCommunication;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class GetChunkHandle extends Thread {

    ProtocolMessage message;
    private DatagramPacket packet_received;
    private ServerSocket s;

    public GetChunkHandle(ProtocolMessage message, DatagramPacket pr){
        this.message = message;
        this.packet_received = pr;
    }

    @Override
    public void run(){
        System.out.println("GETCHUNK fileId:\"" + message.getFileId() + "\" chunkNo:\""+message.getChunkNo()+"\"");
        //check if a chunk has been received
        if (VolatileDatabase.getChunkMemory.indexOf(message.getFileId()+message.getChunkNo())>=0) {
            //send message and remove table entry for fileID+chunkNo
            try {
                if (message.getVersion().equals("1.1")) {
                    //System.out.println("GetChunkHandle - MESSAGE IS 1.1");
                    //ServerSocket tcp_ss = new ServerSocket(5678);
                    //String address = InetAddress.getLocalHost().getHostAddress();
                    //Socket cs = new Socket(packet_received.getAddress(), 5678);
                    s = new ServerSocket(0);
                    s.setSoTimeout(100);
                    System.out.println("Sent address is:" + InetAddress.getLocalHost().getHostAddress());
                    System.out.println("Sent port is:" + s.getLocalPort());
                    message.setBody((InetAddress.getLocalHost().getHostAddress() + ":" + s.getLocalPort()).getBytes());
                } else {
                    message.setBody(FileHandler.getChunk(message.getFileId(), Short.valueOf(message.getChunkNo())));
                }
                System.out.println("SENDING CHUNK BACK");
                message.setMsgType(ProtocolMessage.PossibleTypes.CHUNK);
                message.setSenderId(Integer.toString(Peer.peerID));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Dispatcher.sendRecover(message.toCharArray());
            VolatileDatabase.getChunkMemory.remove(message.getFileId() + message.getChunkNo());
            if (message.getVersion().equals("1.1")){
                try {
                    Socket dataSocket = s.accept();
                    DataOutputStream  dataStream = new DataOutputStream(dataSocket.getOutputStream());
                    System.out.println("Chunk No:"+message.getChunkNo());
                    byte[] data = FileHandler.getChunk(message.getFileId(),Short.valueOf(message.getChunkNo()));
                    System.out.println("chunk Size:"+data.length);
                    System.out.println(new String(data));
                    dataStream.writeInt(data.length);
                    dataStream.flush();
                    dataStream.write(data);
                    System.out.println("written:"+dataStream.size());
                    dataSocket.shutdownOutput();
                    while (!dataSocket.isOutputShutdown());
                    dataStream.close();
                    dataSocket.close();
                    s.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("No connection established to send chunk data");
                }
            }
        }else{
            System.out.println("Chunk Already Received.");
        }
    }
}
