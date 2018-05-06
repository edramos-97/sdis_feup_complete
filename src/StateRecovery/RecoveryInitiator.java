package StateRecovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RecoveryInitiator extends Thread {

    // FileId -> chunkNO
    // chunkNo >=0 && < 1000000   -> stored chunk
    // chunkNo >=1000000 && < 2000000  -> remove chunk
    // chunkNo == -1  -> backed up file
    // chunkNo == -2  -> deleted file backed up
    // chunkNo == -3  -> deleted stored file
    private static ConcurrentHashMap<String, List<Integer>> recoveryData = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<Integer>> volatileData = new ConcurrentHashMap<>();

    public static void dump(){
        //TODO - broadcast volatile data if not empty
        volatileData.forEach((k,v)->{
            System.out.print("fileId:"+k+" | Chunks:[");
            for (Integer i: v) {
                System.out.print(i + ",");
            }
            System.out.println("]");
        });
        volatileData.clear();
    }

    public static void addBackup(String fileName, long date){
        String fileNameAndDate = fileName + ";" + date;
        if(!volatileData.containsKey(fileNameAndDate)){
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-1);
            volatileData.put(fileNameAndDate,temp);
        }
    }

    public static void addDeleteBackup(String fileName, long date){
        String fileNameAndDate = fileName + ";" + date;
        if(volatileData.containsKey(fileNameAndDate)){
            volatileData.remove(fileNameAndDate);
        }else{
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-2);
            volatileData.put(fileNameAndDate,temp);
        }
        //TODO - add entry to volatile data
    }

    public static void addDeleteStored(String fileId){
        volatileData.remove(fileId);
        List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
        temp.listIterator().add(-3);
        volatileData.put(fileId,temp);
        //TODO - add entry to volatile data
    }

    public static void addStored(String fileId, int chunkNo){
        if(volatileData.containsKey(fileId)){
            List<Integer> temp = volatileData.get(fileId);
            if (temp!=null){
                temp.listIterator().add(chunkNo);
            }else{
                System.out.println("Couldn't add store log to volatile database");
            }
            // VolatileData.put(fileId, temp);
        }else {
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(chunkNo);
            volatileData.put(fileId, temp);
        }
        //TODO - add entry to volatile data
    }

    public static void addRemoved(String fileId,int chunkNo){
        //TODO - add entry to volatile data
        if(volatileData.containsKey(fileId)){
            List<Integer> temp = volatileData.get(fileId);
            if (temp!=null){
                temp.listIterator().add(chunkNo + 1000000);
            }else{
                System.out.println("Couldn't add removed log to volatile database");
            }
            // VolatileData.put(fileId, temp);
        }else {
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(chunkNo + 1000000);
            volatileData.put(fileId, temp);
        }

    }
}
