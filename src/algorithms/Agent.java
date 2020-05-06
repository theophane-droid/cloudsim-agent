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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * this class centralize
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
        System.out.println("action");
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
        return migrationMap!=null;
    }
    /**
     * Apply agent on an AgentHost
     * @param sw concerned AgentSwitch
     * @return true if modification has been done on AgentSwitch
     */
    public boolean action(AgentSwitch sw){
        System.out.println("action from switch " + sw.getId()) ;
        boolean modification = false;
        List<Pair<Boolean, Port>> list = sw.sortUsedAndUnusedConnexions();
        for(Pair<Boolean, Port> pair: list){
            System.out.println("    connexion to host " + ((Host)pair.getSecond().getReliedObject()).getId() + " : " + pair.getFirst());
        }
        int nbHostUp = 0;
        for(Pair<Boolean, Port> p: list){
            modification = p.getFirst() != p.getSecond().isOpen() || modification;
            if(p.getFirst())
                nbHostUp+=1;
            p.getSecond().setOpen(p.getFirst());
        }
        modification = nbHostUp!=0 != sw.isActive() || modification;
        sw.setIsActive(nbHostUp!=0);
        sw.updateConnexions();
        if(modification){
/*            System.out.println("things has been modified");
            System.out.println("nb Host up : " + nbHostUp);*/
        }
        return modification;
    }
}