package network;

import org.cloudbus.cloudsim.network.datacenter.HostPacket;
import org.cloudbus.cloudsim.network.datacenter.NetworkPacket;

import java.util.ArrayList;
import java.util.List;

public class Agent extends NetworkPacket {
    private List<NetworkPacket> packetsRecieved;
    public Agent(int id, HostPacket pkt2, int vmid, int cloudletid) {
        super(id, pkt2, vmid, cloudletid);
        packetsRecieved = new ArrayList<>();
    }
}
