package simulations;

import algorithms.Action;
import algorithms.Scheduler;
import network.AgentDatacenter;
import network.AgentHost;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.ini4j.Wini;
import utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runner wich allows to run Network simulations
 * @author Théophane Dumas
 */

public abstract class SimulationRunner {
    protected List<AgentHost> hostList;
    protected List<Cloudlet> cloudletList;
    protected List<Vm> vmLists;
    protected Wini ini;
    protected AgentDatacenter agentDatacenter;
    protected DatacenterBroker broker;
    private String name;
    private String workload;
    private String inputFolder;
    private String outputFolder;
    protected int nbHosts;
    protected int nbVms;
    protected int nbCloudlets;
    private boolean printDatacenter;

    public SimulationRunner(){}

    public SimulationRunner(String name,String workload, String inputFolder, String outputFolder, boolean printDatacenter){
        this.name = name;
        this.workload=workload;
        this.inputFolder=inputFolder;
        this.outputFolder=outputFolder;
        this.printDatacenter=printDatacenter;
    }

    /**
     * Init the simulation
     */
    public abstract void init();

    /**
     * Start the simulation
     * @return a map which contains the simulations's result
     */
    public Map<String, Double> start(){
        System.out.println("start");
        broker.submitCloudletList(cloudletList);
        List<Vm> vmLists2 = Utils.copyList(vmLists);
        broker.submitVmList(vmLists);

        if(printDatacenter)
            activeSimulationPrint();
        double lastClock = 0.d;
        try {
            lastClock = CloudSim.startSimulation();
        }
        catch (Exception e){
            System.err.println("An expeption occurs with message : ");
            e.printStackTrace();
            Utils.printDatacenterState(agentDatacenter, CloudSim.clock());
            System.exit(-1) ;
        }
        System.out.println("last clock : " + lastClock);

        List<Cloudlet> newList = broker.getCloudletReceivedList();
        Log.printLine("Received " + newList.size() + " cloudlets");
        CloudSim.stopSimulation();


        Log.setDisabled(false);
        Helper.printCloudletList(cloudletList);
        Pair<Double, Double> result = NetworkHelper.printResults(
                agentDatacenter,
                vmLists2,
                lastClock,
                name,
                Constants.OUTPUT_CSV,
                outputFolder
                );
        Map<String, Double> m = new HashMap();
        m.put("hosts_power",result.getFirst());
        m.put("switchs_power",result.getSecond());
        return m;
    }

    /**
     * This method use a Scheduler to print every 20000 sec
     */
    protected void activeSimulationPrint(){
        System.out.println("active simulation print");
        Action action = new Action() {
            private AgentDatacenter dc = agentDatacenter;
            @Override
            public void action() {
                Utils.printDatacenterState(dc, CloudSim.clock());
            }
        };
        new Scheduler("printer_scheduler", 20, action);
    }
}
