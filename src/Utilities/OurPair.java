package Utilities;


public class OurPair implements Comparable<OurPair> {
    String fileID;
    FileInfo info;

    public OurPair(String fileID, FileInfo fi){
        this.fileID = fileID;
        this.info = fi;
    }

    @Override
    public int compareTo(OurPair pair) {
        return this.info.compareTo(pair.info);
    }

}
