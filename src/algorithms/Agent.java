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
    public Agent(AgentDatacenter agentDatacenter){
        this.agentDatacenter = agentDatacenter;
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
        return migrationMap!=null;
    }
    /**
     * Apply agent on an AgentHost
     * @param sw concerned AgentSwitch
     * @return true if modification has been done on AgentSwitch
     */
    public boolean action(AgentSwitch sw){
        boolean modification = false;
        if(sw.isAccess()){
            List<Pair<Boolean, Port>> m = sw.sortUsedAndUnusedConnexions();
            boolean allPortDown = true;
            for(Pair<Boolean, Port> p: m){
                if(p.getFirst()) {
                    allPortDown = false;
                    break;
                }
            }
            modification = sw.isActive()==allPortDown;
            sw.setIsActive(!allPortDown);
            for (Pair<Boolean, Port> p : m) {
                p.getSecond().setOpen(p.getFirst());
                modification=p.getFirst()!=p.getSecond().isOpen() || modification;
                p.getSecond().setOpen(p.getFirst());
            }
        }
        else if(!sw.isCore()) {
            if (sw.getPowerConsumptionHistory().size() > 0)
                System.out.println("utilization : " + sw.getUtilization() + " => " + sw.getName());
            System.out.println(Vars.DAEMON_SWITCH_LOWER_BOUND);
            if (sw.getUtilization() < Vars.DAEMON_SWITCH_LOWER_BOUND && sw.getPowerConsumptionHistory().size() != 0)
                System.out.println("shut down switch " + sw.getName());
            modification = (sw.getUtilization() < Vars.DAEMON_SWITCH_LOWER_BOUND && sw.getPowerConsumptionHistory().size() != 0) != sw.isActive() || modification;
            sw.setIsActive((sw.getUtilization() >= Vars.DAEMON_SWITCH_LOWER_BOUND && sw.getPowerConsumptionHistory().size() != 0));
        }
        sw.updateConnexions();
        return modification;
    }
}