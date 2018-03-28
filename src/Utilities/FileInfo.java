package Utilities;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;

public class FileInfo implements Serializable, Comparable<FileInfo>{

    private short requiredRepDeg;
    private short repDeg;
    private HashSet<Integer> stored_peers = new HashSet<Integer>();
    private short chunkNo;
    private int size;

    public FileInfo(short requiredRepDeg, short repDeg, short chunkNo, int size) {
        this.requiredRepDeg = requiredRepDeg;
        this.repDeg = repDeg;
        this.chunkNo = chunkNo;
        this.size = size;
    }

    public int getSize(){
        return size;
    }

    public short getRequiredRepDeg() {
        return requiredRepDeg;
    }

    public void setRequiredRepDeg(short requiredRepDeg) {
        this.requiredRepDeg = requiredRepDeg;
    }

    public short getRepDeg() {
        return repDeg;
    }

    public void incrementRepDeg(int peerID) {
        stored_peers.add(peerID);
        this.repDeg = (short) stored_peers.size();
    }

    public short getChunkNo() {
        return chunkNo;
    }

    @Override
    public int compareTo(FileInfo o) {
        return Integer.compare(this.repDeg-this.requiredRepDeg,o.repDeg-o.requiredRepDeg);
    }

    public void print(PrintStream stream) {
        stream.printf("ChunkNo: %-6s|Current Replication degree: %s|Replication degree threshold: %s \n",chunkNo,repDeg,requiredRepDeg);
    }
}
