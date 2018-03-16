package InitiatorCommunication;

import Utilities.FileHandler;

import java.io.File;
import java.security.InvalidParameterException;

public class DiskReclaimRequest extends Thread{

    private int allocGoal;

    public DiskReclaimRequest(int desiredAllocation){
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
            System.out.println("Not enough space available on SavePath location");
        }
        try{
            FileHandler.setAllocation(this.allocGoal);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Allocation successfully set to "+this.allocGoal);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
