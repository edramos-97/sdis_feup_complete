package Utilities;

import java.util.ArrayList;
import java.util.List;

public class StateOfPeer {

    public class StoredFile {

        public class ChunkFile {

            public int chunkNumber;
            public short perceivedReplicationDegree;
            public int sizeKBs;

            public ChunkFile(int cn, short rd, int s){
                chunkNumber = cn;
                perceivedReplicationDegree = rd;
                sizeKBs = s;
            }

        }

        public String fileID;
        public String filePathname;
        public int desiredReplicationDegree;
        public ArrayList<ChunkFile> chunks = new ArrayList<>();


        public StoredFile(String fd, String pn, int rp){
            fileID = fd;
            filePathname = pn;
            desiredReplicationDegree = rp;
        }

        public boolean addChunk(ChunkFile cf){

            for(ChunkFile cftemp : chunks){
                if(cftemp.chunkNumber == cf.chunkNumber){
                    return false;
                }
            }

            chunks.add(cf);
            return true;
        }

    }

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
