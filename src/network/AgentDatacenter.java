package network;

import algorithms.Agent;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.network.datacenter.Switch;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentDatacenter extends PowerDatacenter {
    private Map<Integer, AgentSwitch> agentSwitchs;
    public AgentDatacenter(String name, DatacenterCharacteristics characteristics, PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        agentSwitchs = new HashMap<>();
    }
    @Override
    public void updateCloudletProcessing(){
        if (this.getCloudletSubmitted() != -1.0D && this.getCloudletSubmitted() != CloudSim.clock()) {
            double currentTime = CloudSim.clock();
            if (currentTime > this.getLastProcessTime()) {
                double minTime = this.updateCloudetProcessingWithoutSchedulingFutureEventsForce();
                if (!this.isDisableMigrations()) {
                    Agent a = new Agent(this);
                    for (Host h : getHostList()) {
                        AgentHost agentHost = (AgentHost) h;
                        agentHost.getPacketsToSort().add(new RawPacket(0, agentHost.getId(), null, agentHost.getClass(), a));
                        agentHost.processPackets();
                        agentHost. readRecievedPackets();
                    }
                    for(int id: getAgentSwitchs().keySet()){
                        AgentSwitch sw = getAgentSwitchs().get(id);
                        sw.getPacketsToSort().add(new RawPacket(0, sw.getId(), null, sw.getClass(), a));
                        sw.processPackets(sw.getPacketsToSort());
                        sw.readRecievedPackets();
                    }
                }
            }
            this.setLastProcessTime(currentTime);
        }
        else {
            CloudSim.cancelAll(this.getId(), new PredicateType(41));
            this.schedule(this.getId(), this.getSchedulingInterval(), 41);
        }
        for(int i: agentSwitchs.keySet()){
            agentSwitchs.get(i).updatePowerConsumption();
        }
    }
    public void publicIncrementMigrationCount(){
        incrementMigrationCount();
    }

    public void publicSend(int entityId, double delay, int cloudSimTag, Object data){
        send(entityId, delay, cloudSimTag, data);
    }

    public Map<Integer, AgentSwitch> getAgentSwitchs() {
        return agentSwitchs;
    }

    @Override
    public PowerVmAllocationPolicyMigrationAbstract getVmAllocationPolicy() {
        return (PowerVmAllocationPolicyMigrationAbstract)super.getVmAllocationPolicy();
    }
}
