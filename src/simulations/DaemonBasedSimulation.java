package simulations;

import algorithms.Action;
import algorithms.AgentPowerLocalRegressionPolicyMigration;
import algorithms.Scheduler;
import com.opencsv.CSVWriter;
import network.AgentDatacenter;
import network.AgentHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.ini4j.Wini;
import utils.Utils;
import utils.Vars;

import java.util.Calendar;
import java.util.List;

/**
 * A class wich allow to run a daemon based simulation
 * @author Théophane Dumas
 */
public class DaemonBasedSimulation extends SimulationRunner{
    private float daemonMipsConsumption;;
    public DaemonBasedSimulation(){
        super();
    }

    /**
     * This constructor is called dynamically in Main
     * @param ini
     */
    public DaemonBasedSimulation(Wini ini) {
        this(ini.get("simulation","name", String.class),
                "",
                "",
                "",
                ini.get("datacenter","nb_hosts", int.class),
                ini.get("datacenter","nb_vms", int.class),
                ini.get("cloudlets","nb_cloudlets", int.class),
                ini.get("DaemonBased","mips_consuption", float.class),
                ini.get("DaemonBased","lower_bound_ratio", float.class),
                ini.get("DaemonBased","upper_bound_ratio", float.class),
                ini.get("simulation","print_datacenter",boolean.class));
        this.ini = ini;
    }

    public DaemonBasedSimulation(String name, String workload, String inputFolder, String outputFolder,
                               int nbHost, int nbVms, int nbCloudlets, float daemonMipsConsumption,
                                 float lower_bound, float upper_bound, boolean printDatacenter) {
        super(name, workload, inputFolder, outputFolder, printDatacenter);
        this.nbHosts = nbHost;
        this.nbVms = nbVms;
        this.nbCloudlets = nbCloudlets;
        this.daemonMipsConsumption = daemonMipsConsumption;
        Vars.DAEMON_UPPER_BOUND=upper_bound;
        Vars.DAEMON_LOWER_BOUND=lower_bound;
    }
    public void init() {
        Log.setDisabled(true);
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(nbHosts);
        broker = Helper.createBroker();
        vmLists = Helper.createVmList(broker.getId(), nbVms);
        cloudletList = Utils.createTheProperCloudletList(broker.getId(), nbCloudlets, vmLists, ini);
        System.out.println("cloudlet list size = " + cloudletList.size());
        // * we set the Scheduler cloudlet list (very important)
        Scheduler.cloudletsList = Utils.copyList(cloudletList);
        agentDatacenter = NetworkHelper.createDatacenter("datacenter0", hostList, AgentPowerLocalRegressionPolicyMigration.createAgentPolicy(hostList), cloudletList);
        NetworkHelper.buildNetwork(agentDatacenter);
        // * now we start the daemon on every host
        for(AgentHost host: hostList){
            host.startDaemon();
        }
        // * on every switchs too
        for(int id: agentDatacenter.getAgentSwitchs().keySet()){
            agentDatacenter.getAgentSwitchs().get(id).startDaemon();
        }
        // * we sent the agent once
        agentDatacenter.sendAgent();

        Action action = new Action() {
            private AgentDatacenter dc = agentDatacenter;
            @Override
            public void action() {
                dc.updateCloudletProcessing();
            }
        };
        new Scheduler("scheduler", 1000, action);
    }
}
