package Utilities;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;

public class StateOfPeer implements Serializable, Remote {

    private int PeerID;
    private ArrayList<StoredFile> files = new ArrayList<>();

    public StateOfPeer(int pid){
        PeerID = pid;
    }

    public StateOfPeer(){
        PeerID = 0;
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
        System.out.println();
        System.out.println("State of Peer with ID: " + PeerID + " is: ");
        System.out.println();
        for(StoredFile sf : files){
            System.out.println("File with ID : " + sf.fileID + " at " + sf.filePathname);
            System.out.println("File has " + sf.chunks.size() + " chunks. With desired replication degree: " + sf.desiredReplicationDegree);
            System.out.println("Chunks:");
            for (ChunkFile c : sf.chunks){
                System.out.println("Chunk-> " + c.chunkNumber + " with size " + c.sizeKBs +"KBs and with replication degree " + c.perceivedReplicationDegree);
                System.out.println("Saved on: ");
                for (Integer i : c.peers_stored){
                    System.out.print("- " + i + " ");
                }
            }
            System.out.println();
        }
    }

}
