package simulations;

import algorithms.Action;
import algorithms.AgentPowerLocalRegressionPolicyMigration;
import algorithms.Scheduler;
import network.AgentDatacenter;
import network.AgentHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.ini4j.Wini;
import utils.Utils;

import java.util.Calendar;
import java.util.List;

/**
 * This class allow user to run a simulation without Agent
 * @author Théophane Dumas
 */
public class NoAgentSimulation extends SimulationRunner {
    public NoAgentSimulation(Wini ini){
        this(ini.get("simulation","name", String.class),
                "",
                "",
                "",
                ini.get("datacenter","nb_hosts", int.class),
                ini.get("datacenter","nb_vms", int.class),
                ini.get("cloudlets","nb_cloudlets", int.class),
                ini.get("simulation","print_datacenter",boolean.class));
        this.ini=ini;
    }
    public NoAgentSimulation(String simulationName, String workload, String inputFolder, String outputFolder,int nbHosts, int nbVms, int nbCloudlets, boolean printDatacenter) {
        super(simulationName, workload, inputFolder, outputFolder, printDatacenter);
        this.nbHosts=nbHosts;
        this.nbVms=nbVms;
        this.nbCloudlets=nbCloudlets;
    }

    @Override
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

        // * we sent the agent once
        //agentDatacenter.sendAgent();
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
