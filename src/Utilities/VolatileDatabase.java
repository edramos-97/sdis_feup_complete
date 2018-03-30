package Utilities;


import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class VolatileDatabase {

    private static ConcurrentHashMap<String, List<FileInfo>> database = new ConcurrentHashMap<>();
    // fileID -> ( chunkNo -> replicationDegree , , ,)

    public static ConcurrentHashMap<String, String> backed2fileID = new ConcurrentHashMap<>();
    // filename_backup -> fileID
    // by examining this we can know if the file has been altered because repeated filenames
    // will have equal fileIDs if the same file and different otherwise...

    public static ConcurrentHashMap<String, Integer[]> restoreMemory = new ConcurrentHashMap<>();
    // fileID -> [last chunkNo received ; size of the last chunk received for that fileID]
    // used to determine if the last chunk of a file has been restored

    public static  Vector<String> getChunkMemory = new Vector<>();
    // fileID+chunkNo -> boolean
    // used to check if a chunk message has already been sent for the respective getchunk request;

    private VolatileDatabase(){

    }

    public static void add_chunk_stored(String fileID, short chunkNumber, int stored_peerID){
        if(database.containsKey(fileID)){

            List<FileInfo> data = database.get(fileID);

            boolean found = false;
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    found = true;
                    fi.incrementRepDeg(stored_peerID);
                    break;
                }
            }

            /*
            if(!found){
                FileInfo fi = new FileInfo(chunkNumber);
                fi.incrementRepDeg(stored_peerID);
                data.add(fi);
            }*/


        }else{
            //https://stackoverflow.com/questions/4903611/java-list-sorting-is-there-a-way-to-keep-a-list-permantly-sorted-automatically?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            /*
            List<FileInfo> entry = create_array();

            FileInfo fi = new FileInfo(chunkNumber);
            fi.incrementRepDeg(stored_peerID);

            entry.add(fi);
            database.put(fileID, entry);
            */
        }
    }

    public static FileInfo getReplication(String fileID, short chunkNumber){
        if(database.containsKey(fileID)){

            List<FileInfo> data = database.get(fileID);
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    return fi;
                }
            }

        }

        return null;
    }

    public static void chunkDeleted(String fileID, short chunkNumber, int peerID){
        if(database.containsKey(fileID)){

            List<FileInfo> data = database.get(fileID);
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    fi.decrementReplicationDegree(peerID);
                }
            }

        }
    }

    private static ArrayList<FileInfo> create_array(){
        return new ArrayList<FileInfo>(){
            public boolean add(FileInfo fi){
                int index = Collections.binarySearch(this, fi);
                if (index < 0)
                    index = ~index;
                super.add(index, fi);
                return true;
            }
        };
    }

    public static void add_chunk_putchunk(String fileID, short chunkNumber, short requiredReplication, int size){
        if(database.containsKey(fileID)){

            List<FileInfo> data = database.get(fileID);

            boolean found = false;
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    found = true;
                    fi.setRequiredRepDeg(requiredReplication);
                    break;
                }
            }

            if(!found){
                FileInfo fi = new FileInfo(requiredReplication, (short) 0, chunkNumber, size);
                data.add(fi);
            }


        }else{
            //https://stackoverflow.com/questions/4903611/java-list-sorting-is-there-a-way-to-keep-a-list-permantly-sorted-automatically?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            List<FileInfo> entry = create_array();

            FileInfo fi = new FileInfo(requiredReplication, (short) 0, chunkNumber, size);

            entry.add(fi);
            database.put(fileID, entry);
        }
    }

    public static boolean check_has_chunk(String fileID, short chunkNumber){
        if(database.containsKey(fileID)){
            List<FileInfo> data = database.get(fileID);
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    return true;
                }
            }
        }
        return false;
    }

    public static short get_rep_degree(String fileID, short chunkNumber){
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

    public static void print(PrintStream stream){
        for (Map.Entry<String, List<FileInfo>> pair : database.entrySet()) {
            stream.println("File: "+pair.getKey());
            for (FileInfo info : pair.getValue()) {
                info.print(stream);
            }
            stream.println("");
        }
    }

    public static ConcurrentHashMap<String, List<FileInfo>> get_database(){
        return database;
    }

    public static ConcurrentHashMap<String, String> get_backed_up(){
        return backed2fileID;
    }


}
