package InitiatorCommunication;

import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.FileInfo;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DeleteRequest implements Runnable{

    File file;

    public DeleteRequest(String path){
        file = new File(path);
    }

    @Override
    public void run() {
        if (file.isDirectory()){
            System.out.println("DELETE request path cannot point to a directory, terminating request...");
        }else{
            ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.DELETE);

            message.setFileId(FileHandler.getFileId(file));

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

            //todo check this
            /*FileInfo[] infoArray = VolatileDatabase.get_database().entrySet()
            for (FileInfo info : infoArray) {
                VolatileDatabase.deletedFiles.put(message.getFileId(),)
            }
            VolatileDatabase.backed2fileID.remove(message.getFileId());*/

            if(FileHandler.removeFolder(file)){
                System.out.println("DELETE could not delete local copy of the requested file, terminating request...");
            }
        }
    }
}
