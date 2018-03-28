package Utilities;

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