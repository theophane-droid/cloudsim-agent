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
import org.cloudbus.cloudsim.examples.power.random.RandomHelper;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationLocalRegression;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumMigrationTime;
import org.ini4j.Wini;

import java.util.Calendar;
import java.util.List;

/**
 * Class wich run a simulation with an agent time based
 * @author Théophane Dumas
 **/
public class TimeBasedSimulation extends SimulationRunner {
    private int nbHosts;
    private int nbVms;
    private int nbCloudlets;
    private double repeatingTime;

    public TimeBasedSimulation(){
        super();
    }
    /**
     * This constructor is called dynamically in Main
     * @param ini
     */
    public TimeBasedSimulation(Wini ini) {
        this(ini.get("simulation","name", String.class),
                "",
                "",
                "",
                ini.get("datacenter","nb_hosts", int.class),
                ini.get("datacenter","nb_vms", int.class),
                ini.get("datacenter","nb_cloudlets", int.class),
                ini.get("agent","repeating_time", double.class));
    }

    public TimeBasedSimulation(String name, String workload, String inputFolder, String outputFolder,
                               int nbHost, int nbVms, int nbCloudlets, double repeatingTime) {
        super(name, workload, inputFolder, outputFolder);
        this.nbHosts = nbHost;
        this.nbVms = nbVms;
        this.nbCloudlets = nbCloudlets;
        this.repeatingTime = repeatingTime;
    }

    @Override
    public void init() throws Exception {
        Log.setDisabled(true);
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(nbHosts);
        System.out.println(hostList);
        broker = Helper.createBroker();
        vmLists = Helper.createVmList(broker.getId(), nbVms);
        System.out.println("size = " + vmLists.size());
        cloudletList = NetworkHelper.createCloudletList(broker.getId(), nbCloudlets, vmLists);
        agentDatacenter = NetworkHelper.createDatacenter("datacenter0", hostList,new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, new PowerVmSelectionPolicyMinimumMigrationTime(), 0.7D), cloudletList);
        NetworkHelper.buildNetwork(nbHosts, agentDatacenter);
        Action action = new Action() {
            private AgentDatacenter dc = agentDatacenter;
            @Override
            public void action() {
                dc.sendAgent();
            }
        };

        new Scheduler("scheduler", repeatingTime, action);
    }
}
