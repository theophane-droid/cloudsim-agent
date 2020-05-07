package simulations;

import algorithms.Action;
import algorithms.Agent;
import algorithms.Scheduler;
import network.AgentDatacenter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import network.AgentHost;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationLocalRegression;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumMigrationTime;

import java.util.Calendar;
import java.util.List;

/**
 * Class wich run the first simulation process based on planetlab cloudlets
 * @author Théophane Dumas*/
public class TimeBasedSimulation extends SimulationRunner {

    public TimeBasedSimulation(String name, String workload, String inputFolder, String outputFolder) {
        super(name, workload, inputFolder, outputFolder);
    }

    @Override
    public void init() throws Exception {
        Log.setDisabled(true);
        int numberHost = 100;
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(numberHost);
        System.out.println(hostList);
        broker = Helper.createBroker();
        vmLists = Helper.createVmList(broker.getId(), 200);
        cloudletList = NetworkHelper.createCloudletList(broker.getId(), 100);
        agentDatacenter = NetworkHelper.createDatacenter("datacenter0", hostList,new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, new PowerVmSelectionPolicyMinimumMigrationTime(), 0.7D));
        NetworkHelper.buildNetwork(numberHost, agentDatacenter);
        Action action = new Action() {
            private AgentDatacenter dc = agentDatacenter;
            @Override
            public void action() {
                dc.sendAgent();
            }
        };

        new Scheduler("scheduler", 1000, action);
    }
}
