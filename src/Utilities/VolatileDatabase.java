package Utilities;


import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class VolatileDatabase {

    public static ConcurrentHashMap<String, List<FileInfo>> database = new ConcurrentHashMap<>();
    // fileID -> ( chunkNo -> replicationDegree , , ,)
    public static ConcurrentHashMap<String, String> backed2fileID = new ConcurrentHashMap<>();
    // filename_backup -> fileID
    // by examining this we can know if the file has been altered because repeated filenames
    // will have equal fileIDs if the same file and different otherwise...
    private VolatileDatabase(){

    }

    public void add_chunk(String fileID, short chunkNumber, long chunkSize, short requiredReplication){
        if(database.containsKey(fileID)){

            List<FileInfo> data = database.get(fileID);

            boolean found = false;
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    found = true;
                    fi.incrementRepDeg();
                    break;
                }
            }

            if(!found){
                FileInfo fi = new FileInfo(requiredReplication, (short) 1, chunkNumber, chunkSize);
                data.add(fi);
            }


        }else{

            List<FileInfo> entry = new ArrayList<FileInfo>(){
                public boolean add(FileInfo fi){
                    int index = Collections.binarySearch(this, fi);
                    if (index < 0)
                        index = ~index;
                    super.add(index, fi);
                    return true;
                }
            };

            FileInfo fi = new FileInfo(requiredReplication, (short) 1, chunkNumber, chunkSize);

            entry.add(fi);
            database.put(fileID, entry);
        }
    }

    public short get_rep_degree(String fileID, short chunkNumber){
        if(database.containsKey(fileID)){
            List<FileInfo> data = database.get(fileID);

            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    return fi.getRepDeg();
                }
            }
        }
        return 0;
    }

    // from:
    // https://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
    public static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance( "SHA-256" );

        // Change this to UTF-16 if needed
        md.update( input.getBytes( StandardCharsets.UTF_8 ) );
        byte[] digest = md.digest();

        return String.format( "%064x", new BigInteger( 1, digest ) );
    }

}
