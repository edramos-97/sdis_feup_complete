package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class GetChunkHandle extends Thread {

    ProtocolMessage message;
    private DatagramPacket packet_received;

    public GetChunkHandle(ProtocolMessage message, DatagramPacket pr){
        this.message = message;
        this.packet_received = pr;
    }

    @Override
    public void run(){
        System.out.println("GETCHUNK fileId:\"" + message.getFileId() + "\" chunkNo:\""+message.getChunkNo()+"\"");
        //check if a chunk has been received
        if (VolatileDatabase.getChunkMemory.indexOf(message.getFileId()+message.getChunkNo())>=0){
            //send message and remove table entry for fileID+chunkNo
            try {

                // TODO RESTORE-ENHANCEMENT
                if(message.getVersion().equals("1.1")){
                    //System.out.println("GetChunkHandle - MESSAGE IS 1.1");
                    //ServerSocket tcp_ss = new ServerSocket(5678);
                    //String address = InetAddress.getLocalHost().getHostAddress();

                    Socket cs = new Socket(packet_received.getAddress(), 5678);

                    ProtocolMessage aux_message = new ProtocolMessage(ProtocolMessage.PossibleTypes.CHUNK);
                    aux_message.setFileId(message.getFileId());
                    aux_message.setChunkNo(message.getChunkNo());
                    aux_message.setBody(FileHandler.getChunk(message.getFileId(),Short.valueOf(message.getChunkNo())));
                    aux_message.setMsgType(ProtocolMessage.PossibleTypes.CHUNK);
                    aux_message.setSenderId(Integer.toString(Peer.peerID));
                    aux_message.setVersion("1.1");

                    DataOutputStream dos = new DataOutputStream(cs.getOutputStream());
                    dos.write(aux_message.toCharArray());
                    //dos.writeBytes("\n");
                    cs.close();

                }else{
                    message.setBody(FileHandler.getChunk(message.getFileId(),Short.valueOf(message.getChunkNo())));
                }

                System.out.println("SENDING CHUNK BACK");
                message.setMsgType(ProtocolMessage.PossibleTypes.CHUNK);
                message.setSenderId(Integer.toString(Peer.peerID));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Dispatcher.sendRecover(message.toCharArray());
            
            VolatileDatabase.getChunkMemory.remove(message.getFileId()+message.getChunkNo());
        }else{
            System.out.println("Chunk Already Received.");
        }
    }
}
