package Executables;

import MulticastThreads.MulticastChanelControl;
import MulticastThreads.MulticastChanelData;
import MulticastThreads.MulticastChanelRecovery;
import PackageRMI.Control;
import PackageRMI.ControlInterface;
import Utilities.FileHandler;
import Utilities.VolatileDatabase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.*;

public class Peer {

    public static int peerID = 0;
    public static String VERSION = "1.0";
    public static int MAX_CONCURRENCY = 4;
    private static final RejectedExecutionHandler rejectedExecutionHandler = (r, executor) -> {
        System.out.println("DELAYING SOME THREAD");
        int delay = 500 + new Random().nextInt(500);
        ((ScheduledThreadPoolExecutor)executor).schedule(r, delay, TimeUnit.MILLISECONDS);
    };
    public static ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),rejectedExecutionHandler);

    public static void main(String[] args) {
        if(args.length < 7){
            System.out.println("one of two usages:");
            System.out.println("usage -> java Executables.Peer PeerID  <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
            //System.out.println("usage_2 -> java Executables.Peer PeerID  <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <sub_protocol> <opnd_1> <opnd_2>");
            System.out.println("exiting...");
            return;
        }

        peerID = Integer.parseInt(args[0]);
        MulticastChanelControl mcc;
        MulticastChanelData mcd;
        MulticastChanelRecovery mcr;

        // trying to create main threads
        try {
            mcc = new MulticastChanelControl(args[1],args[2],args[3],args[4],args[5],args[6], peerID);
            mcd = new MulticastChanelData(args[1],args[2],args[3],args[4],args[5],args[6], peerID);
            mcr = new MulticastChanelRecovery(args[1],args[2],args[3],args[4],args[5],args[6], peerID);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating main threads!");
            System.out.println("exiting...");
            return;
        }

        // start main threads
        mcc.start();
        mcd.start();
        mcr.start();

        //RMI setup
        try {
            Control control_rmi = new Control();
            ControlInterface control_rmi_stub = (ControlInterface) UnicastRemoteObject.exportObject(control_rmi, 0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            String name = "peer"+peerID;
            registry.rebind(name, control_rmi_stub);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("Error in RMI setup.");
        }

        FileHandler.startPeerFileSystem();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("This was the state of my internals...");
            System.out.println("RESTORE INITIATED IS EMPTY: "+VolatileDatabase.restoreMemory.isEmpty());
            System.out.println("GETCHUNK WAITING ANSWER IS EMPTY: "+VolatileDatabase.getChunkMemory.isEmpty());

            System.out.println("::::::::::::::: PEER INFO :::::::::::::::");
            System.out.println("MAX ALLOCATED SPACE AVAILABLE: "+FileHandler.getFreeSpace()/1000.0 +"kB");
            System.out.println("MAX AVAILABLE STORAGE SPACE: "+FileHandler.getAllocatedSpace()/1000.0+"kB");
            System.out.println("USED STORAGE SPACE: "+FileHandler.getDiskUsage()/1000.0+"kB\n");
            VolatileDatabase.print(System.out);

            try {
                FileOutputStream fout = new FileOutputStream(FileHandler.dbserPath);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                VolatileDatabase.writeObject(oos);
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        VolatileDatabase.populateExisting();
        VolatileDatabase.networkUpdate();

        System.out.println("INITIATING PROGRAM");
    }
}
