package InitiatorCommunication;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.security.InvalidParameterException;

public class DiskReclaimRequest extends Thread{

    private long allocGoal;

    public DiskReclaimRequest(long desiredAllocation){
        if (desiredAllocation<0){
            throw new InvalidParameterException("DiskReclaimRequest - Disk space allocated must be greater or equal to 0.");
        }

        this.allocGoal = desiredAllocation*1000;
    }

    @Override
    public String toString() {
        return "Disk space reclaim terminated.";
    }

    @Override
    public void run() {
        if(FileHandler.getAvailableSpace()<this.allocGoal){
            System.out.println("DiskReclaimRequest - RECLAIM not enough space available on SavePath location.");
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


            } catch (Exception e) {
                e.printStackTrace();
            }

            //SEND MESSAGE
            Dispatcher.sendControl(message.toCharArray());
        }
        System.out.println("Allocation successfully set to "+this.allocGoal);
    }
}
