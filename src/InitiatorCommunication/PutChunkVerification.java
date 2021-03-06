package InitiatorCommunication;

import Executables.Peer;
import Utilities.Dispatcher;
import Utilities.File_IO_Wrapper;
import Utilities.ProtocolMessage;
import Utilities.VolatileDatabase;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public class PutChunkVerification implements Runnable {
    private final static short TIMEOUT = 1000;
    private final static short MAX_TRIES = 5;
    private final Closeable openFile;

    private int tryNo;
    private ProtocolMessage message;

    PutChunkVerification(int tryNo, File_IO_Wrapper info){
        this.tryNo = tryNo;
        this.message = info.getMessage();
        this.openFile = info.getFile();
    }

    @Override
    public void run() {
        if(tryNo>=MAX_TRIES){
            System.out.println("PutChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" finished unsuccessfully after "+MAX_TRIES+" tries.");
        }else{
            // Checking replication degree of file
            short current_rep_degree = VolatileDatabase.get_rep_degree(message.getFileId(), Short.valueOf(message.getChunkNo()));
            if (current_rep_degree < message.getReplicationDeg()){

                // Resending message to everyone, hoping someone new accepts the chunk.
                System.out.println("PutChunk for fileID:\""+message.getFileId()+"\" chunkNo:"+message.getChunkNo()+" failed try number "+tryNo+". Retrying...");

                Dispatcher.sendData(message.toCharArray());

                VolatileDatabase.add_chunk_putchunk(message.getFileId(), Short.valueOf(message.getChunkNo()), message.getReplicationDeg(), -1);
            }else{
                int nextChunkNo = Integer.parseInt(message.getChunkNo())+Peer.MAX_CONCURRENCY;
                if (nextChunkNo < message.getThreadNo()){
                    try {
                        Peer.threadPool.submit(new PutChunkRequest(message.getFile(),(short)nextChunkNo, Short.toString(message.getReplicationDeg()).charAt(0),message.getThreadNo(),message.getVersion()));
                    } catch (Exception e) {
                        System.out.println("PutChunkVerification - Error: " + e.toString());
                        //e.printStackTrace();
                    }
                }
                System.out.println("PUTCHUNK completed for fileId:\""+message.getFileId()+"\" Chunk:\""+message.getChunkNo()+"\"");

                //VolatileDatabase.deleteChunkEntry(message.getFileId(), Short.valueOf(message.getChunkNo()));
                return;
            }

            int delay = (int)Math.pow(2,tryNo)*TIMEOUT;
            tryNo++;
            Peer.threadPool.schedule(this,delay, TimeUnit.MILLISECONDS);
        }
    }
}