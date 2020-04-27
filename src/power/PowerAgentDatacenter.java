package power;

import org.apache.commons.math3.geometry.spherical.twod.Edge;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

import java.util.List;

/**
 * This class allow user to meseasure the consumption of switchs and host and use network abilities.
 * @author Théophane Dumas
 */
public class PowerAgentDatacenter extends NetworkDatacenter {
    private boolean hasBeenInitialized = false;
    public PowerAgentDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }
    @Override
    protected void updateCloudletProcessing() {
        super.updateCloudletProcessing();
        for(Host host : getHostList())
            ((PowerAgentHost)host).updatePowerConsumption();
        for(int id: getEdgeSwitch().keySet())
            ((PowerAgentSwitch)getEdgeSwitch().get(id)).updatePowerConsumption();
    }
    public double getTotalUtilisation(){
        double total=0;
        for(Host host : getHostList()){
            List<Double> history = ((PowerAgentHost)host).getPowerUtilisationHistory();
            for(double d: history)
                total+=d;
        }
        for(int id: getEdgeSwitch().keySet()){
            List<Double> history = ((PowerAgentSwitch)getEdgeSwitch().get(id)).getPowerUtilisationHistory();
            for(double d: history)
                total+=d;
        }
        return total;
    }
}
