package Utilities;

import InitiatorCommunication.DiskReclaimRequest;
import InitiatorCommunication.PutChunkRequest;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class FileHandler {

    private static final int CHUNK_SIZE = 65535;
    private static final String EXTENSION = ".txt";
    private static String savePath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"testFolder"+File.separator;
    private static long allocatedSpace;

    public static void main(String[] args) throws InterruptedException {
        /*ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(4)){
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                System.out.print(r.toString());
            }
        };
        long size = FileHandler.getSize(new File(savePath+"1.txt"));
        float chunkTotal = (float)size / CHUNK_SIZE;
        System.out.println("division number:"+chunkTotal);
        for (short i = 0; i < chunkTotal; i++) {
            try {
                executor.execute(new PutChunkRequest(savePath+"1.txt",i,'3'));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        while (executor.getActiveCount()!=0);
        executor.execute(new DiskReclaimRequest(300000));
        executor.shutdown();
        while (!executor.isTerminated());*/

//        byte[] data;
//        try {
//            for (int i = 0; i < 2; i++) {
//                data = splitFile(savePath+"1.txt",i);
//                saveChunk("fileId-No3",data,Integer.toString(i));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(getDiskUsage(savePath)/1000);
//
//        System.out.println(hasFile("fileId-No1")?"TRUE":"FALSE");
    }

    public static long getAllocatedSpace() {
        return allocatedSpace;
    }

    private static void setAllocatedSpace(long allocatedSpace) {
        FileHandler.allocatedSpace = allocatedSpace;
    }

    /**
     * Function used to save a file chunk
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
            out.write(data);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }finally{
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Verifies if a file is backed up in the current backup storage
     * @param fileId - char array with the file id
     * @param chunkNo - number of the chunk to be checked
     * @return Returns true if the file is backed up, false otherwise
     */
    public static boolean hasChunk(String fileId,short chunkNo){
        FileFilter directoryFileFilter = File::isDirectory;

        File directory = new File(savePath);

        File[] paths = directory.listFiles(directoryFileFilter);
        if(paths != null){
            for (File file:paths) {
                if(file.getName().equals(fileId)) {
                    return hasChunk(file,chunkNo);
                }
            }
        }
        return false;
    }

    /**
     * Verifies if a file chunk is present in the file being analysed
     * @param file - File object pointing to a backed up file
     * @param chunkNo - chunk number to be checked
     * @return True if the chunk is present, false otherwise
     */
    private static boolean hasChunk(File file,short chunkNo){
        File[] chunks = file.listFiles();
        return chunks!= null && chunkNo < chunks.length && chunks[chunkNo].getName().equals(chunkNo+EXTENSION);
    }

    /**
     * Function used to verify if a file is backed up
     * @param fileId - char array with the file id
     * @return Returns true if the file is backed up, false otherwise
     */
    private static boolean hasFile(String fileId){
        FileFilter directoryFileFilter = File::isDirectory;

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
     * Function that returns the current backed up files size on disk
     * @return Returns the size in bytes of the used disk space
     */
    private static long getDiskUsage() {

        Path path = Paths.get(savePath);

        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.out.println("Skipped: " + file + " (" + exc + ")");
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null)
                        System.out.println("Had trouble traversing: " + dir + " (" + exc + ")");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }
        return size.get();
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (openFile != null) {
                openFile.close();
            }
        }

        return chunk;
    }

    /**
     * Function to obtain a file size in bytes
     * @param file - path of the file
     * @return Returns the file length in bytes or 0 if the file doesn't exist or the path point to a directory
     */
    private static long getSize(File file){
        if (file.isDirectory()){
            return 0;
        }else{
            return file.length();
        }
    }

    /**
     * Method used to retreive the number of chunck a file should be divided in
     * @param path - path to the file
     * @return returns the number of chunks a file can be divided in
     * @throws Exception - Throws exception if the path specified doesn't point to a file
     */
    public static short getChunkNo(String path) throws Exception {
        File file = new File(path);
        long size = getSize(file);
        if (size==0){
            throw new Exception("File path indicated is either a directory or doesn't exist");
        }else{
            return (short)Math.ceil((float)size / CHUNK_SIZE);
        }
    }

    /**
     * Encodes file name and date created to protocol standard
     * @param filePath - Path of the file to be encoded
     * @return - Returns the encoded file id or null if the path doesn't point to a file
     */
    public static String getFileId(String filePath){
        File file = new File(filePath);
        if(!file.isDirectory()){
            String fileInfo = file.getName()+file.lastModified();
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(fileInfo.getBytes());
                String hashedName = DatatypeConverter.printHexBinary(hash);
                System.out.println(hashedName.toLowerCase());
                return hashedName.toLowerCase();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Retrieves a chunk of a given file from the backed up storage
     * @param fileId - Id of the file to be retreived
     * @param chunkNo - Number of the file chunk
     * @return Returns the data of the chunk
     * @throws Exception - Throws Exception when the file id is not backed up or the chunk does not have the correct size
     */
    public static byte[] getChunk(String fileId,short chunkNo) throws Exception {
        if (!hasFile(fileId)){
            throw new Exception("File with Id:"+fileId+" is not backed up");
        }else{
            byte[] chunk = new byte[CHUNK_SIZE];
            RandomAccessFile file;
            file =  new RandomAccessFile(savePath+fileId+File.pathSeparator+chunkNo+EXTENSION,"r");
            if (file.read(chunk)!=CHUNK_SIZE){
                throw new Exception("File with Id:"+fileId+"has been corrupted on chunk No:"+chunkNo);
            }else{
                return chunk;
            }
        }
    }

    /**
     * Method used to delete a folder
     * @param folder - File object representing the folder to be deleted
     * @return Returns true if the folder is successfully deleted, false otherwise
     */
    private static boolean removeFolder(File folder){
        File[] folderContents = folder.listFiles();
        if (folderContents!=null){
            for (File file : folderContents) {
                if(!removeFolder(file))return false;
            }
        }
        return folder.delete();
    }

    public static long getAvailableSpace(){
        return new File(savePath).getFreeSpace();
    }

    public static File[] setAllocation(long allocSpace){
        if(getDiskUsage()<allocSpace){
            setAllocatedSpace(allocSpace);
            return null;
        }
        File[] removedFiles = new File[]{};

        //TODO choose files to delete and add them to removedFiles array
        /*for (File folder: selectedFiles ) {
            removeFolder(folder);
        }*/

        System.out.println("allocating space");
        return removedFiles;
    }
}
