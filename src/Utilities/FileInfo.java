package Utilities;

import java.io.Serializable;

public class FileInfo implements Serializable, Comparable<FileInfo>{

    private short requiredRepDeg;
    private short repDeg;
    private short chunkNo;
    private long size;

    public FileInfo(short requiredRepDeg, short repDeg, short chunkNo, long size) {
        this.requiredRepDeg = requiredRepDeg;
        this.repDeg = repDeg;
        this.chunkNo = chunkNo;
        this.size = size;
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

    public long getSize() {
        return size;
    }

    public short getChunkNo() {
        return chunkNo;
    }

    @Override
    public int compareTo(FileInfo o) {
        return Integer.compare(this.repDeg-this.requiredRepDeg,o.repDeg-o.requiredRepDeg);
    }
}
