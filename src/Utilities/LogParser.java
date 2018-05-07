package Utilities;

import StateRecovery.RecoveryInitiator;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogParser implements Runnable {

    private String path;

    public LogParser(String filename) {
        path = FileHandler.restorePath + filename;
    }

    @Override
    public void run() {
        /*
        File f = Paths.get(path).toFile();
        if(f == null) {
            System.out.println("Log files do not exist");
            return;
        }
        if(f.isDirectory()){
            System.out.println("Log file is a directory");
            return;
        }*/
        BufferedReader br;
        try {
            FileReader f = new FileReader(path);
            br = new BufferedReader(f);
            if(br == null) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Log file not found, unable to recover data");
            return;
        }
        String line;
        try {
            while((line=br.readLine()) != null) {
                parseLine(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error in parsing logs");
        }
    }

    private void parseLine(String line) {
        String[] lineSplited = line.split("\\|", 2);
        String[] chunks = lineSplited[1].split(",");

        for(String chunk : chunks) {
            switch(chunk) {
                case "-1":
                    List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
                    temp.add(-1);
                    RecoveryInitiator.recoveryData.put(lineSplited[0], temp);
                    break;
                case "-2":
                    RecoveryInitiator.recoveryData.remove(lineSplited[0]);
                    break;
                case "-3":
                    RecoveryInitiator.recoveryData.remove(lineSplited[0]);
                    break;
                default:
                    if(Integer.parseInt(chunk) < 1000000) {
                        addStored(lineSplited[0], Integer.parseInt(chunk));
                    } else {
                        removeStored(lineSplited[0], Integer.parseInt(chunk));
                    }
            }
        }
    }



    private void removeStored(String fileId, int chunkNumber) {
        int chunkNo = chunkNumber - 1000000;
        if(RecoveryInitiator.recoveryData.containsKey(fileId)) {
            if(RecoveryInitiator.recoveryData.get(fileId).contains(chunkNo)) {
                RecoveryInitiator.recoveryData.get(fileId).remove(chunkNo);
            }
        }
    }

    private void addStored(String fileId, int chunkNumber) {
        if(RecoveryInitiator.recoveryData.containsKey(fileId)){
            List<Integer> temp = RecoveryInitiator.recoveryData.get(fileId);
            if (temp!=null){
                temp.listIterator().add(chunkNumber);
            }else{
                System.out.println("Couldn't add store log to volatile database");
            }
            // VolatileData.put(fileId, temp);
        }else {
            List<Integer> temp = Collections.synchronizedList(new ArrayList<>());
            temp.listIterator().add(chunkNumber);
            RecoveryInitiator.recoveryData.put(fileId, temp);
        }
    }


}
