package Utilities;

import java.io.PrintStream;
import java.io.Serializable;

public class FileInfo implements Serializable, Comparable<FileInfo>{

    private short requiredRepDeg;
    private short repDeg;
    private short chunkNo;
    private long size;

    public FileInfo(short requiredRepDeg, short repDeg, short chunkNo) {
        this.requiredRepDeg = requiredRepDeg;
        this.repDeg = repDeg;
        this.chunkNo = chunkNo;
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

    public void incrementRepDeg() {
        this.repDeg += 1;
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
