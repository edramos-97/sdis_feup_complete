package Utilities;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;

public class ChunkFile implements Serializable, Remote {

    public int chunkNumber;
    public short perceivedReplicationDegree;
    public int sizeKBs;
    public ArrayList<Integer> peers_stored = new ArrayList<>();

    public ChunkFile(int cn, short rd, int s){
        chunkNumber = cn;
        perceivedReplicationDegree = rd;
        sizeKBs = s;
    }

    public void addPeer(Integer i){
        peers_stored.add(i);
    }
}