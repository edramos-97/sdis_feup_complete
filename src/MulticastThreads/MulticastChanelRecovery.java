package MulticastThreads;

import java.io.IOException;
import java.net.InetAddress;

public class MulticastChanelRecovery extends MulticastChanel {

    public MulticastChanelRecovery(String mcc_address, String mcc_port, String mcb_address,
                                   String mcb_port, String mcr_address, String mcr_port) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port);

        //joining group
        multicast_recover_socket.joinGroup(InetAddress.getByName(multicast_recover_address));

    }

    @Override
    public void run() {

        // listen on recover

    }

}
