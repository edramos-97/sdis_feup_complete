package Executables;

import MulticastThreads.MulticastChanelControl;
import MulticastThreads.MulticastChanelData;
import MulticastThreads.MulticastChanelRecovery;
import PackageRMI.Control;
import PackageRMI.ControlInterface;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer {

    public static int peerID = 0;
    public static String VERSION = "1.0";
    public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 4, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));

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
/*
        boolean is_initiator = Integer.parseInt(args[1]) == 1;

        if(is_initiator){
            System.out.println("Initiating command given.");
           String subprotocol, operand_1, operand_2;
            if(args.length < 9){
                System.out.println("Subprotocol not given, exiting now.");
                return;
            }
            subprotocol = args[8];
            if(args.length < 10){
                System.out.println("First operand not given, exiting now.");
                return;
            }
            operand_1 = args[9];


            String operand_2 = args[10]
        }*/

//        regular peer:
//          java Executables.Peer 0 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
//        initiator peer:
//          java Executables.Peer 1 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <sub_protocol> <opnd_1> <opnd_2>

    }
}
