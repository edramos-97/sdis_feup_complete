package Utilities;

import java.io.*;
import java.util.ArrayList;

public class FileHandler {

    private static final int CHUNK_SIZE = 65535;

    public static void main(String[] args){
        byte[] data;
        try {
            for (int i = 0; i < 2; i++) {
                data = splitFile(savePath+"1.txt",i);
                saveChunk("fileId-No3",data,Integer.toString(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println(hasFile("fileId-No1")?"TRUE":"FALSE");

    }

    private static String savePath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"testFolder"+File.separator;

    /**
     *
     * @param fileId - File identifier
     * @param data - File chunk data
     * @param chunkNo - File chunk sequence number
     */
    public static void saveChunk(String fileId, byte[] data, String chunkNo) throws IOException {
        FileOutputStream out = null;

        try {
            String path = savePath+fileId+File.separator+chunkNo+".txt";
            System.out.println(path);
            File chunk = new File(path);
            chunk.getParentFile().mkdirs();
            out = new FileOutputStream(chunk,false);
            for (byte c : data) {
                out.write(c);
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }finally{
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Function used to verify if a file is backed up
     * @param fileId - char array with the file id
     * @return Returns true if the file is backed up, false otherwise
     */
    public static boolean hasFile(String fileId){
        FileFilter directoryFileFilter = file -> file.isDirectory();

        File directory = new File(savePath);

        File[] paths = directory.listFiles(directoryFileFilter);
        if(paths != null){
            for (File file:paths) {
                if(file.getName().equals(fileId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Function used to obtain a chunk of a file
     * @param path - path of the file
     * @param chunkNo - File chunk sequence number
     * @return byte array with the file bytes
     * @throws IOException - File couldn't be closed
     */
    public static byte[] splitFile(String path, int chunkNo) throws IOException {
        byte[] chunk = new byte[CHUNK_SIZE];

        RandomAccessFile openFile = null;
        try {
            openFile = new RandomAccessFile(new File(path),"r");
            openFile.seek(chunkNo*CHUNK_SIZE);
            int bytesRead = openFile.read(chunk);
            if (bytesRead != CHUNK_SIZE) {
                byte[] smallerData = new byte[bytesRead];
                System.arraycopy(chunk, 0, smallerData, 0, bytesRead);
                chunk = smallerData;
            }
            System.out.println(bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (openFile != null) {
                openFile.close();
            }
        }


        return chunk;
    }
}
