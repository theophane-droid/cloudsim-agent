package simulations;

import network.AgentDatacenter;
import network.AgentHost;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;

import java.util.List;

/**
 * Runner wich allows to run Network simulations
 * @author Théophane Dumas
 */

public abstract class SimulationRunner {
    protected List<AgentHost> hostList;
    protected List<NetworkCloudlet> cloudletList;
    protected List<Vm> vmLists;
    protected AgentDatacenter agentDatacenter;
    protected DatacenterBroker broker;
    private String name;
    private String workload;
    private String inputFolder;
    private String outputFolder;
    public SimulationRunner(String name,String workload, String inputFolder, String outputFolder){
        this.name = name;
        this.workload=workload;
        this.inputFolder=inputFolder;
        this.outputFolder=outputFolder;
    }

/**
     * Init the simulation
     * @throws Exception*/
    public abstract void init() throws Exception;

/**
     * Start the simulation*/


    public void start(){
        broker.submitVmList(vmLists);
        broker.submitCloudletList(cloudletList);

      //  CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
        double lastClock = CloudSim.startSimulation();
        System.out.println("last clock : " + lastClock);

        List<Cloudlet> newList = broker.getCloudletReceivedList();
        Log.printLine("Received " + newList.size() + " cloudlets");

        CloudSim.stopSimulation();

        NetworkHelper.printResults(
                agentDatacenter,
                vmLists,
                lastClock,
                name,
                Constants.OUTPUT_CSV,
                outputFolder
                );
    }
}
