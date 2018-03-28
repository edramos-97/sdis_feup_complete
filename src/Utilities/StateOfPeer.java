package Utilities;

import java.util.ArrayList;
import java.util.List;

public class StateOfPeer {

    private int PeerID;
    private ArrayList<StoredFile> files = new ArrayList<>();

    public StateOfPeer(int pid){
        PeerID = pid;
    }

    public boolean addFile(StoredFile sf){
        for(StoredFile sftemp : files){
            if(sftemp.fileID.equals(sf.fileID)){
                return false;
            }
        }
        files.add(sf);
        return true;
    }

    public void printState(){
        System.out.println("State of Peer " + PeerID + "is: ");

        // TODO add rest of print function
    }

}
