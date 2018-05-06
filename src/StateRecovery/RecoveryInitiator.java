package StateRecovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RecoveryInitiator extends Thread {

    // FileId -> chunkNO
    // chunkNo >=0    -> stored chunk
    // chunkNo == -1  -> backed up file
    // chunkNo == -2  -> deleted file
    private ConcurrentHashMap<String, List<Integer>> recoveryData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<Integer>> volatileData = new ConcurrentHashMap<>();

    public void dump(){
        //TODO - broadcast volatile data if not empty
        volatileData.forEach((k,v)->{
            System.out.print("fileId:"+k+" | Chunks:[");
            for (Integer i: v) {
                System.out.print(i+',');
            }
            System.out.println();
        });
    }

    public void addBackup(String fileId){
        if(!volatileData.containsKey(fileId)){
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-1);
            volatileData.put(fileId,temp);
        }
        //TODO - add entry to volatile data
    }

    public void addDelete(String fileId){
        if(volatileData.containsKey(fileId)){
            volatileData.remove(fileId);
        }else{
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(-2);
            volatileData.put(fileId,temp);
        }
        //TODO - add entry to volatile data
    }

    public void addStored(String fileId,int chunkNo){
        if(volatileData.containsKey(fileId)){
            List<Integer> temp = volatileData.get(fileId);
            if (temp!=null){
                temp.listIterator().add(chunkNo);
            }else{
                System.out.println("Couldn't add store log to volatile database");
            }
        }else {
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(chunkNo);
            volatileData.put(fileId, temp);
        }
        //TODO - add entry to volatile data
    }

    public void addRemoved(String fileId,int chunkNo){
        //TODO - add entry to volatile data
    }
}
