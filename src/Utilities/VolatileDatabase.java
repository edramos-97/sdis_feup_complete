package Utilities;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

public final class VolatileDatabase {

    public static ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> database = new ConcurrentHashMap<>();
    // fileID -> ( chunkNo -> replicationDegree , , ,)
    public static ConcurrentHashMap<String, String> backed2fileID = new ConcurrentHashMap<>();
    // filename_backup -> fileID
    // by examining this we can know if the file has been altered because repeated filenames
    // will have equal fileIDs if the same file and different otherwise...
    private VolatileDatabase(){

    }

    public void add_chunk(String fileID, Integer chunk_number){
        if(database.containsKey(fileID)){
            ConcurrentHashMap<Integer, Integer> data = database.get(fileID);
            if(data.containsKey(chunk_number)){
                Integer replication_degree = data.remove(chunk_number);
                data.put(chunk_number, replication_degree+1);
            }else {
                data.put(chunk_number, 1);
            }

        }else{
            ConcurrentHashMap<Integer,Integer> entry = new ConcurrentHashMap<>();
            entry.put(chunk_number, 1);
            database.put(fileID, entry);
        }
    }

    public Integer get_rep_degree(String fileID, Integer chunk_number){
        if(database.containsKey(fileID)){
            ConcurrentHashMap<Integer, Integer> data = database.get(fileID);
            if(data.containsKey(chunk_number)){
                return data.get(chunk_number);
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
