package InitiatorCommunication;

import Utilities.FileHandler;
import Utilities.ProtocolMessage;

import java.io.File;
import java.security.InvalidParameterException;

public class DiskReclaimRequest extends Thread{

    private long allocGoal;

    public DiskReclaimRequest(long desiredAllocation){
        if (desiredAllocation<0){
            throw new InvalidParameterException("Disk space allocated must be greater or equal to 0");
        }
        this.allocGoal = desiredAllocation;
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
                message.setFileId(temp[1]);
                message.setChunkNo(temp[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //TODO send message
        }

        System.out.println("Allocation successfully set to "+this.allocGoal);
    }
}
