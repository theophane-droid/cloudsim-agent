package network;

import algorithms.Action;
import algorithms.Agent;
import algorithms.Scheduler;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
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
        createPowerConsumptionScheduler();
    }
    private void createPowerConsumptionScheduler(){
        AgentDatacenter local_dc = this;
        Action action = new Action(){
            private AgentDatacenter dc = local_dc;
            @Override
            public void action() {
                dc.updatePowerConsumption();
            }
        };
        new Scheduler("power_scheduler", 1000, action);
    }
    @Override
    public void updateCloudletProcessing(){

    }


    public void sendAgent(){
        if (this.getCloudletSubmitted() != -1.0D && this.getCloudletSubmitted() != CloudSim.clock()) {
            double currentTime = CloudSim.clock();
            System.out.println("current time : " + currentTime);
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
    }
    public void publicIncrementMigrationCount(){
        incrementMigrationCount();
    }

    public void publicSend(int entityId, double delay, int cloudSimTag, Object data){
        send(entityId, delay, cloudSimTag, data);
    }

    @Override
    protected void send(int entityId, double delay, int cloudSimTag) {
        super.send(entityId, delay, cloudSimTag);
        // * when an agent datacenter event is called, we update the Scheduler counter
        Scheduler.lastDatacenterEvent=CloudSim.clock();
    }

    public Map<Integer, AgentSwitch> getAgentSwitchs() {
        return agentSwitchs;
    }

    @Override
    public PowerVmAllocationPolicyMigrationAbstract getVmAllocationPolicy() {
        return (PowerVmAllocationPolicyMigrationAbstract)super.getVmAllocationPolicy();
    }

    public Pair<Double, Double> getPower2() {
        double power_host=0;
        double power_switch=0;
        for(Host h: getHostList()){
            power_host += ((AgentHost)h).calcTotalPowerConsuption();
        }
        for(int i: agentSwitchs.keySet()){
            power_switch += agentSwitchs.get(i).calcTotalPowerConsuption();
        }
        return new Pair(power_host, power_switch);
    }
    public void updatePowerConsumption(){
        for(int i: getAgentSwitchs().keySet()){
            getAgentSwitchs().get(i).updatePowerConsumption();;
        }
        for(Host host : getHostList()){
            ((AgentHost)host).updatePowerConsumption();
        }
    }
}
