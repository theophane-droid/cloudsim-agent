import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.Calendar;

/**
 * Class wich run the first simulation process based on planetlab cloudlets
 * @author Théophane Dumas
 */
public class SimulationRunner1 extends SimulationRunner {

    public SimulationRunner1(String name,String workload, String inputFolder, String outputFolder) {
        super(name, workload, inputFolder, outputFolder);
    }

    @Override
    public void init() throws Exception {
        CloudSim.init(0, Calendar.getInstance(), false);
        hostList = NetworkHelper.createHostList(10);
        System.out.println(hostList);
        broker = NetworkHelper.createBroker();
        vmLists = NetworkHelper.createVmList(broker.getId(), 20);
        coudletList = NetworkHelper.createCloudletList(broker.getId(), 10);
        networkDatacenter = NetworkHelper.createDatacenter(hostList, new VmAllocationPolicySimple(hostList));
        NetworkHelper.buildNetwork(10, networkDatacenter);
    }
}
