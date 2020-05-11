package simulations;

import algorithms.Action;
import algorithms.Agent;
import algorithms.AgentPowerLocalRegressionPolicyMigration;
import algorithms.Scheduler;
import network.AgentDatacenter;;
import org.cloudbus.cloudsim.Log;
import network.AgentHost;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.ini4j.Wini;
import utils.Utils;

import java.util.Calendar;
import java.util.List;

/**
 * Class wich run a simulation with an agent time based
 * @author Théophane Dumas
 **/
public class TimeBasedSimulation extends SimulationRunner {
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
                ini.get("TimeBased","repeating_time", double.class),
                ini.get("simulation","print_datacenter",boolean.class));
    }

    public TimeBasedSimulation(String name, String workload, String inputFolder, String outputFolder,
                               int nbHost, int nbVms, int nbCloudlets, double repeatingTime, boolean printDatacenter) {
        super(name, workload, inputFolder, outputFolder, printDatacenter);
        this.nbHosts = nbHost;
        this.nbVms = nbVms;
        this.nbCloudlets = nbCloudlets;
        this.repeatingTime = repeatingTime;
    }

    @Override
    public void init() {
        Log.setDisabled(true);
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(nbHosts);
        broker = Helper.createBroker();
        vmLists = Helper.createVmList(broker.getId(), nbVms);
        cloudletList = NetworkHelper.createCloudletList(broker.getId(), nbCloudlets, vmLists);
        // * we set the Scheduler cloudlet list (very important)
        Scheduler.cloudletsList = Utils.copyList(cloudletList);
        agentDatacenter = NetworkHelper.createDatacenter("datacenter0", hostList, AgentPowerLocalRegressionPolicyMigration.createAgentPolicy(hostList), cloudletList);
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
