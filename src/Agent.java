import org.cloudbus.cloudsim.network.datacenter.HostPacket;
import org.cloudbus.cloudsim.network.datacenter.NetworkPacket;

public class Agent extends NetworkPacket {
    public Agent(int id, HostPacket pkt2, int vmid, int cloudletid) {
        super(id, pkt2, vmid, cloudletid);
    }
}
