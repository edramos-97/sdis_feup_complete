package InitiatorCommunication;

import Executables.Peer;
import MulticastThreads.MulticastChanel;
import Utilities.FileHandler;
import Utilities.FileInfo;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

public class DiskReclaimRequest extends Thread{

    private long allocGoal;

    public DiskReclaimRequest(long desiredAllocation){
        if (desiredAllocation<0){
            throw new InvalidParameterException("Disk space allocated must be greater or equal to 0");
        }
        if(desiredAllocation == 0){
            this.allocGoal = FileHandler.getDiskUsage();
        }else{
            this.allocGoal = desiredAllocation*1000;
        }

    }

    @Override
    public String toString() {
        return "Disk space reclaim terminated.";
    }

    @Override
    public void run() {
        if(FileHandler.getAvailableSpace()<this.allocGoal){
            System.out.println("RECLAIM not enough space available on SavePath location");
            return;
        }

        String[] removedFiles = FileHandler.setAllocation(this.allocGoal);

        String[] temp;
        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.REMOVED);
        for (String fileInfo: removedFiles) {
            temp = fileInfo.split(";");
            try {
                message.setFileId(temp[0]);
                message.setChunkNo(temp[1]);

                VolatileDatabase.chunkDeleted(temp[0],Short.valueOf(temp[1]), Peer.peerID);
                /*
                FileInfo info = VolatileDatabase.getInfo(temp[0],Short.valueOf(temp[1]));
                if(info != null)
                    info.decrementReplicationDegree(Peer.peerID);*/

            } catch (Exception e) {
                e.printStackTrace();
            }

            //SEND MESSAGE
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
                System.out.println("error in creating datagram packet...");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("error in sending packet to multicast socket");
                e.printStackTrace();
            }
        }
        System.out.println("Allocation successfully set to "+this.allocGoal);
    }
}
