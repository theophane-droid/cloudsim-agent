package simulations;

import network.AgentHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;

import java.util.Calendar;
import java.util.List;

/**
 * Class wich run the first simulation process based on planetlab cloudlets
 * @author Théophane Dumas*/
public class SimulationRunner1 extends SimulationRunner {

    public SimulationRunner1(String name,String workload, String inputFolder, String outputFolder) {
        super(name, workload, inputFolder, outputFolder);
    }

    @Override
    public void init() throws Exception {
      //  Log.setDisabled(true);
        int numberHost = 20;
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(numberHost);
        System.out.println(hostList);
        broker = Helper.createBroker();
        vmLists = Helper.createVmList(broker.getId(), 1);
        cloudletList = NetworkHelper.createCloudletList(broker.getId(), 10);
        agentDatacenter = NetworkHelper.createDatacenter("datacenter0", hostList, new VmAllocationPolicySimple(hostList));
        NetworkHelper.buildNetwork(numberHost, agentDatacenter);
    }
}
