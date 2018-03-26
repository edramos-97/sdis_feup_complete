package InitiatorCommunication;

import Utilities.FileHandler;

import java.io.File;

public class DeleteRequest implements Runnable{

    File folder;

    public DeleteRequest(String path){
        folder = new File(path);
    }

    @Override
    public void run() {
        if (folder.isDirectory()){
            System.out.println("DELETE request path cannot point to a directory, terminating request...");
        }else{
            if(!FileHandler.removeFolder(folder)){
                System.out.println("DELETE could not delete the requested file, terminating request...");
            }else{
                //TODO send DELETE message
            }
        }
    }
}
