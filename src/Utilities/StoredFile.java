package Utilities;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;

public class StoredFile implements Serializable, Remote{

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