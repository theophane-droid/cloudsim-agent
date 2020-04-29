package network;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerDatacenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentDatacenter extends PowerDatacenter {
    private Map<Integer, AgentSwitch> agentSwitchs;
    public AgentDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        agentSwitchs = new HashMap<>();
    }


    public Map<Integer, AgentSwitch> getAgentSwitchs() {
        return agentSwitchs;
    }
}
