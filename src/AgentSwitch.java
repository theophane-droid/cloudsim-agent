import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

public class AgentSwitch extends EdgeSwitch implements AgentActionner {
    /**
     * Constructor for Edge Switch We have to specify switches that are connected to its downlink
     * and uplink ports, and corresponding bandwidths. In this switch downlink ports are connected
     * to hosts not to a switch.
     *
     * @param name  Name of the switch
     * @param level At which level switch is with respect to hosts.
     * @param dc    Pointer to Datacenter
     */
    public AgentSwitch(String name, int level, NetworkDatacenter dc) {
        super(name, level, dc);
    }
}
