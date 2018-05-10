package InitiatorCommunication;

import Executables.Peer;
import StateRecovery.LogParser;
import StateRecovery.RecoveryInitiator;
import Utilities.Dispatcher;
import Utilities.FileHandler;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.util.concurrent.TimeUnit;

public class GetChunkVerification implements Runnable {

    private static short TIMEOUT = 1000;
    private static short MAX_TRIES = 5;
    private ProtocolMessage message;
    private int tryNo;
    private String fileName;

    GetChunkVerification(int i, ProtocolMessage message, String fileName) {
        this.tryNo = i;
        this.message = message;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        Integer[] info  = VolatileDatabase.restoreMemory.get(message.getFileId());
        //check if current chunk was received

        if(info != null && (info[0]==Integer.parseInt(message.getChunkNo())||fileName.equals("single"))) {
            if (fileName.equals("peer" + Peer.peerID + "recoveryLog")) {
                if (Integer.parseInt(message.getChunkNo()) == RecoveryInitiator.chunkNumber) {
                    System.out.println("LOG RESTORE finished successfully");
                    VolatileDatabase.restoreMemory.remove(message.getFileId());
                    FileHandler.restoreFile(message.getFileId(), fileName + FileHandler.EXTENSION);
                    Peer.threadPool.schedule(new LogParser(fileName), 2000, TimeUnit.MILLISECONDS);
                } else {
                    System.out.println("LOG RESTORE received chunkNo:" + message.getChunkNo() + "finished successfully");
                    Peer.threadPool.submit(new GetChunkRequest(message.getFileId(), (short) (Short.valueOf(message.getChunkNo()) + 1), fileName, message.getVersion()));
                }
            }else {
                if (info[1] != -1 && info[1] < FileHandler.CHUNK_SIZE) {
                    //last chunk of file
                    System.out.println("GETCHUNK for fileID:\"" + message.getFileId() + "\" finished successfully");
                    if(!fileName.equals("single")){
                        VolatileDatabase.restoreMemory.remove(message.getFileId());
                        FileHandler.restoreFile(message.getFileId(), fileName);
                    }
                } else {
                    //get next chunk from file
                    System.out.println("GETCHUNK for fileID:\"" + message.getFileId() + "\" chunkNo:" + message.getChunkNo() + "\" successful");
                    if (!fileName.equals("single"))
                        Peer.threadPool.submit(new GetChunkRequest(message.getFileId(), (short) (Short.valueOf(message.getChunkNo()) + 1), fileName, message.getVersion()));
                }
            }
            if (fileName.equals("single")) {
                VolatileDatabase.add_chunk_putchunk(message.getFileId(), Short.valueOf(message.getChunkNo()), (short) 1, message.body.length);
                VolatileDatabase.add_chunk_stored(message.getFileId(), Short.valueOf(message.getChunkNo()), Integer.valueOf(message.getSenderId()));
            }
            return;
        }
        tryNo++;
        if (tryNo >= MAX_TRIES){
            VolatileDatabase.restoreMemory.remove(message.getFileId());
            System.out.println("GETCHUNK for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" finished unsuccessfully\nDidn't receive a CHUNK message after "+MAX_TRIES+" tries");
        }else{
            System.out.println("GETCHUNK for fileID:\"" + message.getFileId() + "\" chunkNo:" + message.getChunkNo() + " failed to get an answer on try No:" + tryNo + ", retrying...");

            //resend message
            Dispatcher.sendControl(message.toCharArray());

            //check if got chunk again
            Peer.threadPool.schedule(this,1500, TimeUnit.MILLISECONDS);
        }
    }
}
