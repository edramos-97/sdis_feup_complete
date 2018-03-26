package PackageRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControlInterface extends Remote {

    //TODO: fill these
    void say_this(String s) throws RemoteException;

    boolean putChunk(String filePath, char replicationDeg) throws RemoteException;

    boolean getChunk(String fileId, short chunkNo) throws RemoteException;

    boolean delete(String path) throws RemoteException;

    boolean reclaim (long desiredAllocation) throws RemoteException;

    boolean getState () throws RemoteException;


}
