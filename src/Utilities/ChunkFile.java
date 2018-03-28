package Utilities;

import java.io.Serializable;
import java.rmi.Remote;

public class ChunkFile implements Serializable, Remote {

    public int chunkNumber;
    public short perceivedReplicationDegree;
    public int sizeKBs;

    public ChunkFile(int cn, short rd, int s){
        chunkNumber = cn;
        perceivedReplicationDegree = rd;
        sizeKBs = s;
    }

}