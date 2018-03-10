
public class Peer {

    public static int peerID = 0;
    public static String multicast_control_address = "";
    public static String multicast_control_port = "";
    public static String multicast_backup_address = "";
    public static String multicast_backup_port = "";
    public static String multicast_recover_address = "";
    public static String multicast_recover_port = "";

    public static void main(String[] args) {

        if(args.length < 7){
            System.out.println("one of two usages:");
            System.out.println("usage_1 -> java Peer 0 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
            System.out.println("usage_2 -> java Peer 1 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <sub_protocol> <opnd_1> <opnd_2>");
            System.out.println("exiting...");
            return;
        }

        System.out.println("Working...");


//        regular peer:
//          java Peer 0 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
//        initiator peer:
//          java Peer 1 <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port> <sub_protocol> <opnd_1> <opnd_2>

    }
}
