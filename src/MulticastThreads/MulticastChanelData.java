package MulticastThreads;

import java.io.IOException;
import java.net.InetAddress;

public class MulticastChanelData extends MulticastChanel {

    public MulticastChanelData(String mcc_address, String mcc_port, String mcb_address,
                               String mcb_port, String mcr_address, String mcr_port) throws IOException {

        // creating common variables
        super(mcc_address,mcc_port,mcb_address,mcb_port,mcr_address,mcr_port);

        //joining group
        multicast_data_socket.joinGroup(InetAddress.getByName(multicast_data_address));

    }

    @Override
    public void run() {

        // listen on data

    }
}
