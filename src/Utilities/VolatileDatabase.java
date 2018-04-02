package Utilities;


import Executables.Peer;
import MulticastThreads.MulticastChanel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class VolatileDatabase implements Serializable{

    private static ConcurrentHashMap<String, List<FileInfo>> database = new ConcurrentHashMap<>();
    // fileID -> ( chunkNo -> replicationDegree , , ,)

    public static ConcurrentHashMap<String, String> backed2fileID = new ConcurrentHashMap<>();
    // fileID -> filename_backup
    // by examining this we can know if the file has been altered because repeated filenames
    // will have equal fileIDs if the same file and different otherwise...

    private static ConcurrentHashMap<String, HashSet<Integer>> deletedFiles = new ConcurrentHashMap<>();
    // fileID -> number of chunks that have saved the file chunks
    // used for delete messages enhancement

    public static ConcurrentHashMap<String, Integer[]> restoreMemory = new ConcurrentHashMap<>();
    // fileID -> [last chunkNo received ; size of the last chunk received for that fileID]
    // used to determine if the last chunk of a file has been restored

    public static  Vector<String> getChunkMemory = new Vector<>();
    // fileID+chunkNo
    // used to check if a chunk message has already been sent for the respective getchunk request;

    public static Vector<String> removedChunk = new Vector<>();
    // fileID+ChunkNo
    // used to check if another putchunk has already been sent to not overflow with putchunks

    public static synchronized void add_chunk_stored(String fileID, short chunkNumber, int stored_peerID){
        if(database.containsKey(fileID)){

            List<FileInfo> data = database.get(fileID);

            for(Iterator<FileInfo> iteratorFileInfo = data.iterator(); iteratorFileInfo.hasNext();){
                FileInfo fi = iteratorFileInfo.next();
                if(fi.getChunkNo() == chunkNumber){
                    fi.incrementRepDeg(stored_peerID);
                    Collections.sort(data);
                    break;
                }
            }

            /*
            //boolean found = false;
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    //found = true;
                    fi.incrementRepDeg(stored_peerID);
                    break;
                }
            }
            */

            /*if(!found){
                FileInfo fi = new FileInfo(chunkNumber);
                fi.incrementRepDeg(stored_peerID);
                data.add(fi);
            }*/

        }/*else{
            //https://stackoverflow.com/questions/4903611/java-list-sorting-is-there-a-way-to-keep-a-list-permantly-sorted-automatically?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

            List<FileInfo> entry = create_array();

            FileInfo fi = new FileInfo(chunkNumber);
            fi.incrementRepDeg(stored_peerID);

            entry.add(fi);
            database.put(fileID, entry);

        }*/
    }

    public static FileInfo getInfo(String fileID, short chunkNumber){
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

    /*public static ArrayList<FileInfo> getAllFileInfos(String fileID){
        if(database.containsKey(fileID)){
            return (ArrayList<FileInfo>) database.get(fileID);;
        }
        return new ArrayList<>();
    }*/

    public static ArrayList<OurPair> dump(){
        ArrayList<OurPair> result = new ArrayList<OurPair>(){
            public boolean add(OurPair fi){
                int index = Collections.binarySearch(this, fi);
                if (index < 0)
                    index = ~index;
                super.add(index, fi);
                return true;
            }
        };

        for (Map.Entry<String, List<FileInfo>> pair : database.entrySet()) {
            for (FileInfo info : pair.getValue()) {
                result.add(new OurPair(pair.getKey(), info));
            }
        }


        return result;
    }

    public static synchronized void deleteChunkEntry(String fileID, short chunkNumber){
        if(database.containsKey(fileID)){
            List<FileInfo> data = database.get(fileID);
            for (FileInfo fi : data) {
                if (fi.getChunkNo() == chunkNumber) {
                    data.remove(fi);
                    return;
                }
            }
        }
    }

    public static void deleteFile(String fileID){
        HashSet<Integer> result = new HashSet<>();

        for (FileInfo info: database.get(fileID)) {
            result.addAll(info.stored_peers);
        }

        VolatileDatabase.deletedFiles.put(fileID,result);
        VolatileDatabase.database.remove(fileID);
        VolatileDatabase.backed2fileID.remove(fileID);
    }

    public static void chunkDeleted(String fileID, short chunkNumber, int peerID){
        if(database.containsKey(fileID)){
            List<FileInfo> data = database.get(fileID);
            if (Peer.peerID == peerID){
                data.removeIf(fileInfo -> fileInfo.getChunkNo() == chunkNumber);
                if (data.isEmpty()){
                    database.remove(fileID);
                }
            }else{

                for (Iterator<FileInfo> iteratorFileInfo = data.iterator(); iteratorFileInfo.hasNext();){
                    FileInfo fi = iteratorFileInfo.next();
                    if(fi.getChunkNo() == chunkNumber){
                        fi.decrementReplicationDegree(peerID);
                        Collections.sort(data);
                        break;
                    }
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

    public static synchronized void add_chunk_putchunk(String fileID, short chunkNumber, short requiredReplication, int size) {


        if (database.containsKey(fileID)) {
            List<FileInfo> data = database.get(fileID);

            boolean found = false;

            for(Iterator<FileInfo> iteratorFileInfo = data.iterator(); iteratorFileInfo.hasNext();){
                FileInfo fi = iteratorFileInfo.next();
                if (fi.getChunkNo() == chunkNumber){
                    found = true;
                    fi.setRequiredRepDeg(requiredReplication);
                    Collections.sort(data);
                    break;
                }
            }

            if (!found) {
                FileInfo fi = new FileInfo(requiredReplication, (short) 0, chunkNumber, size);
                data.add(fi);
            }

        } else {
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
        stream.println("::::::::::::::: BACKED UP :::::::::::::::");
        for (Map.Entry<String, String> pair : backed2fileID.entrySet()){
            String pathname = backed2fileID.get(pair.getKey());
            stream.println("Backed up from path: " + pathname);
            stream.println("File: "+pair.getKey());
            for (FileInfo info : database.get(pair.getKey())) {
                info.print(stream);
            }
        }

        stream.println("\n::::::::::::::: BACKING UP :::::::::::::::");
        for (Map.Entry<String, List<FileInfo>> pair : database.entrySet()) {

            if(backed2fileID.containsKey(pair.getKey())){
                continue;
            }
            else {
                stream.println("File: "+pair.getKey());
                stream.println("Stored with chunks:");
            }
            for (FileInfo info : pair.getValue()) {
                info.print(stream);
            }
        }

        stream.println("\n::::::::::::::: WAITING FOR DELETE CONFIRMATION :::::::::::::::");
        for (Map.Entry<String, HashSet<Integer>> pair : deletedFiles.entrySet()){
            stream.println("File: "+pair.getKey());
            stream.println("Still backed up on peer(s): " + pair.getValue());
        }
    }

    public static ConcurrentHashMap<String, List<FileInfo>> get_database(){
        return database;
    }

    public static ConcurrentHashMap<String, String> get_backed_up(){
        return backed2fileID;
    }

    public static void populateExisting() {
        try {
            FileInputStream fin = new FileInputStream(FileHandler.dbserPath);
            ObjectInputStream ios = new ObjectInputStream(fin);
            readObject(ios);
            fin.close();
        } catch (IOException e){
            System.out.println("Data base wasn't present, creating an empty one");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeObject(ObjectOutputStream oos) throws IOException{
        oos.writeObject(database);
        oos.writeObject(backed2fileID);
        oos.writeObject(deletedFiles);
        oos.writeObject(new Long(FileHandler.getAllocatedSpace()));
    }

    private static void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException{
        database = (ConcurrentHashMap<String,List<FileInfo>>)ois.readObject();
        backed2fileID = (ConcurrentHashMap<String,String>)ois.readObject();
        deletedFiles =  (ConcurrentHashMap<String, HashSet<Integer>>)ois.readObject();
        FileHandler.setAllocatedSpace(((Long)ois.readObject()).longValue());
    }

    public static void confirmDelete(String fileId, String senderId) {
        HashSet<Integer> peers;
        if((peers = VolatileDatabase.deletedFiles.get(fileId))!=null) {
            peers.remove(Integer.valueOf(senderId));
            if (peers.isEmpty()) {
                VolatileDatabase.deletedFiles.remove(fileId);
                System.out.println("DELETE file with id: \""+fileId+"\" fully deleted from backup");
            }else{
                System.out.println("HERE EMPTY");
            }
        }else{
            System.out.println("HERE null");
        }
    }

    public static boolean needDelete(String fileId, String senderId) {
        HashSet<Integer> peers;
        return (peers = VolatileDatabase.deletedFiles.get(fileId)) != null && peers.contains(Integer.valueOf(senderId));
    }

    public static void networkUpdate(){
        for (Map.Entry<String, List<FileInfo>> pair : database.entrySet()) {
            if(backed2fileID.get(pair.getKey())!=null){
                return;
            }
            ProtocolMessage message = new ProtocolMessage(ProtocolMessage.PossibleTypes.BACKEDUP);
            message.setFileId(pair.getKey());
            byte[] message_bytes = message.toCharArray();

            System.out.println(new String(message_bytes));

            DatagramPacket packet;
            try {
                packet = new DatagramPacket(
                        message_bytes,
                        message_bytes.length,
                        InetAddress.getByName(MulticastChanel.multicast_control_address),
                        Integer.parseInt(MulticastChanel.multicast_control_port));
                MulticastChanel.multicast_control_socket.send(packet);
            } catch (UnknownHostException e) {
                System.out.println("Error in creating datagram packet");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error in sending packet to multicast socket");
                e.printStackTrace();
            }
        }
    }

    public static void restoreDelete(String fileId){
        VolatileDatabase.deletedFiles.remove(fileId);
    }
}
