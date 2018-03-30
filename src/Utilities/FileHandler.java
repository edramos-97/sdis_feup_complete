package Utilities;

import Executables.Peer;
import InitiatorCommunication.PutChunkHandleComplete;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.StandardOpenOption.*;

public class FileHandler {

    public static final int CHUNK_SIZE = 64000;
    public static final int MAX_SIZE_MESSAGE = CHUNK_SIZE + 1024;
    private static final String EXTENSION = ".txt";
    public static String savePath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"sbs_"+ Peer.peerID + File.separator + "backup" +File.separator;
    public static String restorePath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"sbs_"+ Peer.peerID + File.separator + "restore" +File.separator;
    private static long allocatedSpace = 1000000000;

    public static void main(String[] args){
        /*Path filePath = Paths.get(savePath+"/NewFolder/1.txt");
        Path dirPath = Paths.get(savePath+"/NewFolder/");
        try {
            if (Files.notExists(dirPath)){
                Files.createDirectories(dirPath);
            }
            if(Files.notExists(filePath)){
                Files.createFile(filePath);
            }
            AsynchronousFileChannel file = AsynchronousFileChannel.open(filePath,StandardOpenOption.WRITE);
            file.write(ByteBuffer.wrap("ola isto e um array".getBytes()),0);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Getter for the allocated disk space
     * @return Returns the allocated disk space in bytes
     */
    private static long getAllocatedSpace() {
        return allocatedSpace;
    }

    /**
     * Setter for the disk allocation space
     * @param allocatedSpace - Max allocation space in bytes
     */
    private static void setAllocatedSpace(long allocatedSpace) {
        FileHandler.allocatedSpace = allocatedSpace;
    }

    /**
     * Checks if it is possible to save a chunk without exceeding the allocated disk space
     * @return Returns true if there is enough space to save a chunk, false otherwise
     */
    public static boolean canSave(){
        return getAllocatedSpace()>getDiskUsage()+CHUNK_SIZE;
    }

    /**
     * Function used to save a file chunk after a putChunk message is received
     * @param message - received putChunk message
     */
    public static void saveChunk(ProtocolMessage message,String type){
        Path dirPath;
        Path filePath;
        switch (type){
            case "backup":
                dirPath = Paths.get(savePath+message.getFileId());
                filePath = Paths.get(savePath+message.getFileId()+ File.separator+message.getChunkNo()+EXTENSION);
                break;
            case "restore":
                dirPath = Paths.get(restorePath+message.getFileId());
                filePath = Paths.get(restorePath+message.getFileId()+ File.separator+message.getChunkNo()+EXTENSION);
                break;
            default:
                System.out.println("SaveChunk type was unknown, not saving...");
                return;
        }
        try {
            if(Files.notExists(dirPath)){
                Files.createDirectories(dirPath);
            }
            AsynchronousFileChannel file = AsynchronousFileChannel.open(filePath,WRITE,CREATE,TRUNCATE_EXISTING);
            file.write(ByteBuffer.wrap(message.body),0,new File_IO_Wrapper(message,file),new PutChunkHandleComplete());
        } catch (IOException e) {
            e.printStackTrace();
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
                    System.out.println("GOT HERE");
                    return hasChunk(file,chunkNo);
                }
            }
        }
        return false;
    }

    /**
     * Verifies if a file chunk is present in the file being analysed
     * @param folder - File object pointing to a backed up file
     * @param chunkNo - chunk number to be checked
     * @return True if the chunk is present, false otherwise
     */
    private static boolean hasChunk(File folder,short chunkNo){
        File[] chunks = folder.listFiles();
        if (chunks != null){
            for (File file :chunks) {
                if(file.getName().equals(chunkNo+EXTENSION))
                    return true;
            }
        }
        return false;
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
    public static long getDiskUsage() {

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
     * @deprecated
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
     * @throws InvalidParameterException - Throws exception if the path specified doesn't point to a file
     */
    public static short getChunkNo(String path) throws InvalidParameterException {
        File file = new File(path);
        long size = getSize(file);
        if (size==0){
            throw new InvalidParameterException("File path indicated is either a directory or doesn't exist");
        }else{
            if(size%CHUNK_SIZE!=0){
                return (short)((size/CHUNK_SIZE)+1);
            }else{
                return (short)((size/CHUNK_SIZE)+2);
            }
        }
    }

    /**
     * Encodes file name and date created to protocol standard
     * @param file - File object of the file identifier to be encoded
     * @return - Returns the encoded file id or null if the path doesn't point to a file
     */
    public static String getFileId(File file){
        if(!file.isDirectory()){
            String fileInfo = file.getName()+file.lastModified();
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(fileInfo.getBytes());
                String hashedName = DatatypeConverter.printHexBinary(hash);
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
            file =  new RandomAccessFile(savePath+fileId+File.separator+chunkNo+EXTENSION,"r");
            /*if (file.read(chunk)!=CHUNK_SIZE){
                throw new Exception("File with Id:"+fileId+"has been corrupted on chunk No:"+chunkNo);
            }else{
                return chunk;
            }*/
            file.read(chunk);
            return chunk;
        }
    }

    /**
     * Method used to delete a folder
     * @param folder - File object representing the folder to be deleted
     * @return Returns false if the folder is successfully deleted, true otherwise
     */
    public static boolean removeFolder(File folder){
        File[] folderContents = folder.listFiles();
        if (folderContents!=null){
            for (File file : folderContents) {
                if(removeFolder(file))return false;
            }
        }
        return !folder.delete();
    }

    public static void restoreFile(String fileId,String fileName){
        Path outputPath = Paths.get(restorePath+fileName);
        try {
            AsynchronousFileChannel file = AsynchronousFileChannel.open(outputPath,WRITE,CREATE,TRUNCATE_EXISTING);
            File[] chunks = new File(restorePath+fileId).listFiles();
            if (chunks == null){
                System.out.println("RESTORE error in getting file chunks");
                file.close();
                return;
            }
            for (File chunk : chunks) {
                try {
                    byte[] chunkBytes = new byte[FileHandler.CHUNK_SIZE];
                    AsynchronousFileChannel openFile = AsynchronousFileChannel.open(chunk.toPath(), READ);
                    openFile.read(ByteBuffer.wrap(chunkBytes), 0, Integer.parseInt(chunk.getName().split(EXTENSION)[0]),
                            new CompletionHandler<Integer, Integer>() {
                                @Override
                                public void completed(Integer result, Integer attachment) {
                                    file.write(ByteBuffer.wrap(Arrays.copyOfRange(chunkBytes, 0, result)), attachment * FileHandler.CHUNK_SIZE);
                                    try {
                                        openFile.close();
                                        if (attachment == chunks.length - 1) {
                                            file.close();
                                            FileHandler.removeFolder(new File(restorePath+fileId));
                                            System.out.println("ENDED FILE RESTORE");
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void failed(Throwable exc, Integer attachment) {
                                    System.out.println("ERROR IN RESTORE FILE READCHUNK");
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getAvailableSpace(){
        return new File(savePath).getFreeSpace();
    }

    /**
     * Decides what files should be deleted to achieve the amount of space allocated
     * @param allocSpace - size to be allocated in bytes
     */
    public static String[] setAllocation(long allocSpace){
        long diskUsage = getDiskUsage();
        ArrayDeque<String> removedFiles = new ArrayDeque<String>();
        if(diskUsage<allocSpace){
            setAllocatedSpace(allocSpace);
        }else{
            long deleteAmount = diskUsage-allocSpace;
            System.out.println(deleteAmount);

            ArrayList<OurPair> DBdump = VolatileDatabase.dump();
            for (OurPair pair: DBdump) {
                if(deleteAmount <= 0)
                    break;

                System.out.println(pair);
                long sizeOfChunk = pair.info.getSize();

                removedFiles.add(pair.fileID+";"+pair.info.getChunkNo());
                removeFolder(new File(savePath+pair.fileID+File.separator+pair.info.getChunkNo()+EXTENSION));

                deleteAmount -= sizeOfChunk;
            }
            //TODO get all backed files, sort by desiredRepDeg-repDeg, start deleting until deletions
            //make for the difference of deleted space and allocated space, possibly output deleted fileChunks
        }
        return removedFiles.toArray(new String[]{});
    }

    public static String getPath(String fileId, short chunkNo) {
        return savePath+fileId+File.separator+chunkNo+EXTENSION;
    }
}
