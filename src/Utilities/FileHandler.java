package Utilities;

import java.io.*;
import java.util.ArrayList;

public class FileHandler {

    public static void main(String[] args){
        try {
            for (int i = 0; i < 2; i++) {
                System.out.println(new String(splitFile(savePath+"1.txt",i)));
                System.out.println(":::::::::::::::::::::::::::::::::::::");
                System.out.println(":::::::::::::::::::::::::::::::::::::");
                System.out.println(":::::::::::::::::::::::::::::::::::::");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String savePath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"testFolder"+File.separator;


    /**
     *
     * @param fileId - File identifier
     * @param data - File chunk data
     * @param chunkNo - File chunk sequence number
     * @return True if the chunk was saved successfully, false otherwise.
     * @throws IOException - Default IOException
     */
    public static boolean saveChunk(String fileId, String data, char[] chunkNo) throws IOException {
        FileOutputStream out = null;

        try {
            String path = savePath+ new String(chunkNo)+".txt";
            System.out.println(path);
            File chunk = new File(path);
            if (!chunk.getParentFile().mkdirs()){
                throw new IOException("Could not make path for chunk no:"+new String(chunkNo)+" of file:"+fileId);
            }
            out = new FileOutputStream(chunk,false);
            for (char c : data.toCharArray()) {
                out.write(c);
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }finally{
            if (out != null) {
                out.close();
            }
        }
        return true;
    }

    public boolean backupFile(String path){
        return true;
    }

    public boolean hasFile(char[] fileId){

        return true;
    }

    public boolean checkFileIntegrity(char[] fileId){
        return true;
    }

    public static byte[] splitFile(String path, int chunkNo) throws IOException {
        byte[] chunk = new byte[65535];

        RandomAccessFile openFile = new RandomAccessFile(new File(path),"r");

        openFile.seek(chunkNo*65535);

        openFile.read(chunk);

        return chunk;
    }
}
