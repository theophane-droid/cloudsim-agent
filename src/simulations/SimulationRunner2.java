package simulations;

import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import power.PowerAgentDatacenter;

import java.util.Calendar;

/**
 * Class wich run the first simulation process based on planetlab cloudlets
 * @author Théophane Dumas
 */
public class SimulationRunner2 extends SimulationRunner {

    public SimulationRunner2(String name, String workload, String inputFolder, String outputFolder) {
        super(name, workload, inputFolder, outputFolder);
    }

    @Override
    public void init() throws Exception {
        CloudSim.init(0, Calendar.getInstance(), false);
        hostList = PowerNetworkHelper.createHostList(10);
        System.out.println(hostList);
        broker = PowerNetworkHelper.createBroker();
        vmLists = PowerNetworkHelper.createVmList(broker.getId(), 20);
        coudletList = PowerNetworkHelper.createCloudletList(broker.getId(), 10);
        networkDatacenter = PowerNetworkHelper.createDatacenter(hostList, new VmAllocationPolicySimple(hostList));
        PowerNetworkHelper.buildNetwork(10, networkDatacenter);
    }
}
