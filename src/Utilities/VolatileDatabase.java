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
    private VolatileDatabase(){

    }

    public static void add_chunk(String fileID, short chunkNumber, short requiredReplication){
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
                FileInfo fi = new FileInfo(requiredReplication, (short) 1, chunkNumber);
                data.add(fi);
            }


        }else{
            //https://stackoverflow.com/questions/4903611/java-list-sorting-is-there-a-way-to-keep-a-list-permantly-sorted-automatically?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            List<FileInfo> entry = new ArrayList<FileInfo>(){
                public boolean add(FileInfo fi){
                    int index = Collections.binarySearch(this, fi);
                    if (index < 0)
                        index = ~index;
                    super.add(index, fi);
                    return true;
                }
            };

            FileInfo fi = new FileInfo(requiredReplication, (short) 1, chunkNumber);

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
}
