package PackageRMI;

import Utilities.StateOfPeer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControlInterface extends Remote {

    //TODO: fill these
    void say_this(String s) throws RemoteException;

    boolean putChunk(String filePath, char replicationDeg, boolean enhanced) throws RemoteException;

    boolean getChunk(String filePath, boolean enhanced) throws RemoteException;

    boolean delete(String path, boolean enhanced) throws RemoteException;

    boolean reclaim (long desiredAllocation) throws RemoteException;

    StateOfPeer getState() throws RemoteException;


}
