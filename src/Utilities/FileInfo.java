package Utilities;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;

public class FileInfo implements Serializable, Comparable<FileInfo>{

    private short requiredRepDeg;
    private short repDeg;
    public HashSet<Integer> stored_peers = new HashSet<Integer>();
    private short chunkNo;
    private int size;

    public FileInfo(short requiredRepDeg, short repDeg, short chunkNo, int size) {
        this.requiredRepDeg = requiredRepDeg;
        this.repDeg = repDeg;
        this.chunkNo = chunkNo;
        this.size = size;
    }

    public FileInfo(short chunkNo) {
        this.chunkNo = chunkNo;

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

    public void decrementReplicationDegree(int peerID){
        stored_peers.remove(peerID);
        this.repDeg = (short) stored_peers.size();
    }

    public short getChunkNo() {
        return chunkNo;
    }

    @Override
    public int compareTo(FileInfo o) {
        int compare_result = -1*Integer.compare(this.repDeg-this.requiredRepDeg,o.repDeg-o.requiredRepDeg);

        if(compare_result == 0){
            return Integer.compare(o.size, this.size);
        }

        return compare_result;
    }

    public void print(PrintStream stream) {
        stream.printf("ChunkNo: %-6s|Current Replication degree: %s|Replication degree threshold: %s |Size: %-6f\n",chunkNo,repDeg,requiredRepDeg, size/1000.0);
        stream.print("Saved on: ");
        for(Integer i : stored_peers){
            stream.print("- " + i + " ");
        }
        stream.println();
    }
}
