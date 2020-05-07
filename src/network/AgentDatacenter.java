package network;

import algorithms.Action;
import algorithms.Agent;
import algorithms.Scheduler;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.Switch;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AgentDatacenter extends PowerDatacenter {
    private Map<Integer, AgentSwitch> agentSwitchs;
    private List<Cloudlet> cloudletList;
    public AgentDatacenter(String name, DatacenterCharacteristics characteristics, PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, List cloudletList) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        agentSwitchs = new HashMap<>();
        createPowerConsumptionScheduler();
        this.cloudletList = cloudletList;
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
        if (CloudSim.clock() < 0.111D || CloudSim.clock() > this.getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
            List<? extends Host> list = this.getVmAllocationPolicy().getHostList();
            double smallerTime = 1.7976931348623157E308D;

            for(int i = 0; i < list.size(); ++i) {
                Host host = (Host)list.get(i);
                double time = host.updateVmsProcessing(CloudSim.clock());
                if (time < smallerTime) {
                    smallerTime = time;
                }
            }

            if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01D) {
                smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01D;
            }

            if (smallerTime != 1.7976931348623157E308D) {
                this.schedule(this.getId(), smallerTime - CloudSim.clock(), 41);
            }

            this.setLastProcessTime(CloudSim.clock());
        }
    }


    public void sendAgent(){
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

    @Override
    protected void processVmDestroy(SimEvent ev, boolean ack) {
        //super.processVmDestroy(ev, ack);
        //System.out.println("vm destroy");
    }
}
