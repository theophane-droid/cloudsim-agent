package algorithms;

import network.AgentDatacenter;
import network.AgentHost;
import network.AgentSwitch;
import network.Port;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import utils.Vars;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represent the java Agent
 * @author Théophane Dumas
 */
public class Agent {
    private AgentDatacenter agentDatacenter;
    private double injectionTime;
    public static double total_running_host_time;
    public static double total_running_switch_time;
    public Agent(AgentDatacenter agentDatacenter){
        this.agentDatacenter = agentDatacenter;
        injectionTime=CloudSim.clock();
    }

    /**
     * Apply agent on an AgentHost
     * @param host
     * @return true if modification has been done
     */
    public boolean action(AgentHost host){
        List<Map<String, Object>> migrationMap = agentDatacenter.getVmAllocationPolicy().optimizeAllocation(host.getVmList());
        double currentTime = CloudSim.clock();
        if (migrationMap != null) {
            Iterator i$ = migrationMap.iterator();

            while(i$.hasNext()) {
                Map<String, Object> migrate = (Map)i$.next();
                Vm vm = (Vm)migrate.get("vm");
                PowerHost targetHost = (PowerHost)migrate.get("host");
                PowerHost oldHost = (PowerHost)vm.getHost();
                if (oldHost == null) {
                    Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started", currentTime, vm.getId(), targetHost.getId());
                } else {
                    Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started", currentTime, vm.getId(), oldHost.getId(), targetHost.getId());
                }

                targetHost.addMigratingInVm(vm);
                agentDatacenter.publicIncrementMigrationCount();
                agentDatacenter.publicSend(agentDatacenter.getId(), (double)vm.getRam() / ((double)targetHost.getBw() / 16000.0D), 35, migrate);
            }
        }
        host.agentAsBeenRunning=true;
        total_running_host_time+=CloudSim.clock()-injectionTime;
        return migrationMap!=null;
    }
    /**
     * Apply agent on an AgentHost
     * @param sw concerned AgentSwitch
     * @return true if modification has been done on AgentSwitch
     */
    public boolean action(AgentSwitch sw){
        AgentSwitch ag1 = agentDatacenter.getSwitchByName("aggregation1");
        AgentSwitch ag2 = agentDatacenter.getSwitchByName("aggregation2");
        AgentSwitch ag3 = agentDatacenter.getSwitchByName("aggregation3");
        AgentSwitch ag4 = agentDatacenter.getSwitchByName("aggregation4");
        if(sw.isOverUtilized()){
            if(sw==ag1 && !ag2.isActive())
                ag2.setIsActive(true);
            if(sw==ag2 && !ag1.isActive())
                ag1.setIsActive(true);
            if(sw==ag3 && !ag4.isActive())
                ag4.setIsActive(true);
            if(sw==ag4 && !ag3.isActive())
                ag3.setIsActive(true);
        }
        else if(sw.isUnderUtilized()){
            if(sw==ag1 && ag2.isActive() && !ag2.isOverUtilized())
                ag1.setIsActive(false);
            if(sw==ag2 && ag1.isActive() && !ag1.isOverUtilized())
                ag2.setIsActive(false);
            if(sw==ag3 && ag4.isActive() && !ag4.isOverUtilized())
                ag3.setIsActive(false);
            if(sw==ag4 && ag3.isActive() && !ag3.isOverUtilized())
                ag4.setIsActive(false);
        }
        total_running_switch_time+=CloudSim.clock()-injectionTime;
        return false;
    }
}