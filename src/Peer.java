import MulticastThreads.MulticastChanelControl;
import MulticastThreads.MulticastChanelData;
import MulticastThreads.MulticastChanelRecovery;

import java.io.IOException;

public class Peer {

    public static int peerID = 0;

    public static void main(String[] args) {

        if(args.length < 8){
            System.out.println("one of two usages:");
            System.out.println("usage_1 -> java Peer PeerID 0 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
            System.out.println("usage_2 -> java Peer PeerID 1 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <sub_protocol> <opnd_1> <opnd_2>");
            System.out.println("exiting...");
            return;
        }

        peerID = Integer.parseInt(args[0]);
        MulticastChanelControl mcc;
        MulticastChanelData mcd;
        MulticastChanelRecovery mcr;

        // trying to create main threads
        try {
            mcc = new MulticastChanelControl(args[2],args[3],args[4],args[5],args[6],args[7]);
            mcd = new MulticastChanelData(args[2],args[3],args[4],args[5],args[6],args[7]);
            mcr = new MulticastChanelRecovery(args[2],args[3],args[4],args[5],args[6],args[7]);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating main threads!");
            System.out.println("exiting...");
            return;
        }

        //
        mcc.start();
        mcd.start();
        mcr.start();

        boolean is_initiator = Integer.parseInt(args[1]) == 1;

        if(is_initiator){
            //do stuff
        }

//        regular peer:
//          java Peer 0 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
//        initiator peer:
//          java Peer 1 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <sub_protocol> <opnd_1> <opnd_2>

    }
}
