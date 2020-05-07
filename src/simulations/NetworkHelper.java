package simulations;

import network.AgentDatacenter;
import network.AgentHost;
import network.AgentSwitch;
import network.Port;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.random.RandomConstants;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import power.AgentSwitchPowerModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class wich Help to build simulations quickly
 * @author Théophane Dumas
 */
public class NetworkHelper {

    /**
     * Create a NetworkDatacenter
     * @param hostList the datacenters host list
     * @param vmAllocationPolicy
     * @return the NetworkDatacenter created
     */
    public static NetworkDatacenter createDatacenter(
            List<AgentHost> hostList,
            VmAllocationPolicy vmAllocationPolicy)  {
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch,
                os,
                vmm,
                hostList,
                time_zone,
                cost,
                costPerMem,
                costPerStorage,
                costPerBw);

        NetworkDatacenter datacenter = null;

        try {
            Class datacenterClass = NetworkDatacenter.class;
            datacenter = (NetworkDatacenter) datacenterClass.getConstructor(
                    String.class,
                    DatacenterCharacteristics.class,
                    VmAllocationPolicy.class,
                    List.class,
                    Double.TYPE).newInstance(
                    "Datacenter",
                    characteristics,
                    vmAllocationPolicy,
                    new LinkedList<Storage>(),
                    Constants.SCHEDULING_INTERVAL);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return datacenter;
    }

    /**
     * Add switches to the datacenter
     * @param numhost
     * @param dc
     */
    public static void buildNetwork(int numhost, AgentDatacenter dc) {

        int length = (int)Math.ceil(numhost/NetworkConstants.EdgeSwitchPort);
        AgentSwitch agentSwitch[] = new AgentSwitch[length];

        for (int i = 0; i < length; i++) {
            agentSwitch[i] = new AgentSwitch(dc,24, "Edge_"+i, AgentSwitchPowerModel.CISCO_2960X, 4000);
            dc.getAgentSwitchs().put(agentSwitch[i].getId(), agentSwitch[i]);
        }
        for(int i=0; i<length; i++){
            if(i<length-1) {
                agentSwitch[i].getUpSwitchConnexions().add(new Port(true, agentSwitch[i+1]));
            }
            else{
                agentSwitch[i].getUpSwitchConnexions().add(new Port(true, agentSwitch[0]));
            }
            agentSwitch[i].updateConnexions();
            System.out.println("uplink : " + i + " => " + agentSwitch[i].uplinkswitches);
            dc.getAgentSwitchs().put(agentSwitch[i].getId(), agentSwitch[i]);;
        }
        for (Host hs : dc.getHostList()) {
            AgentHost hs1 = (AgentHost) hs;
            hs1.meanTraffic = NetworkConstants.BandWidthEdgeHost;
            int switchnum = (int) (hs.getId() / NetworkConstants.EdgeSwitchPort);

        //    agentSwitch[switchnum].hostlist.put(hs.getId(), hs1);
            hs1.setSw(agentSwitch[switchnum]);
            agentSwitch[switchnum].getHostConnexions().add(new Port(true, hs1));
            agentSwitch[switchnum].updateConnexions();
        }
        for(Host h1 :  dc.getHostList()){
            AgentHost h= (AgentHost)h1;
            h.setBwConsumption(500);
        }

    }

    /**
     * Print the results of the simulations
     * @param datacenter datacenter to print
     * @param vmList
     * @param lastClock
     * @param experimentName
     * @param outputCsv
     * @param outputFolder
     */
    public static void printResults(AgentDatacenter datacenter, List<Vm> vmList, double lastClock, String experimentName, boolean outputCsv, String outputFolder) {
        Helper.printResults(datacenter, vmList, lastClock, experimentName, outputCsv, outputFolder);
        Log.setDisabled(false);
        Log.printLine("\n\n********Power consumption*******");
        Pair<Double, Double> power_result = datacenter.getPower2();
        Log.printLine("host consumption : " + power_result.getFirst()/(3600*1000) + " kWh");
        Log.printLine("switch consumption : " + power_result.getSecond()/(3600*1000) + " kWh");
        Log.printLine("total: " + (power_result.getFirst()+power_result.getSecond())/(3600*1000) + " kWh");
    }

    public static List<AgentHost> createHostList(int hostsNumber) {
        ArrayList<AgentHost> hostList = new ArrayList();

        for(int i = 0; i < hostsNumber; ++i) {
            int hostType = i % 2;
            List<Pe> peList = new ArrayList();

            for(int j = 0; j < Constants.HOST_PES[hostType]; ++j) {
                peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
            }

            hostList.add(new AgentHost(i, new RamProvisionerSimple(Constants.HOST_RAM[hostType]), new BwProvisionerSimple(10000000000L), 100000000000L, peList, new VmSchedulerTimeSharedOverSubscription(peList), Constants.HOST_POWER[hostType]));
        }

        return hostList;
    }

    /**
     *
     * @param name
     * @param hostList
     * @param vmAllocationPolicy
     * @return
     * @throws Exception
     */
    public static AgentDatacenter createDatacenter(String name,  List<AgentHost> hostList, PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0D;
        double cost = 3.0D;
        double costPerMem = 0.05D;
        double costPerStorage = 0.001D;
        double costPerBw = 0.0D;
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        AgentDatacenter datacenter = null;

        try {
            datacenter = AgentDatacenter.class.getConstructor(String.class, DatacenterCharacteristics.class, PowerVmAllocationPolicyMigrationAbstract.class, List.class, Double.TYPE).newInstance(name, characteristics, vmAllocationPolicy, new LinkedList(), 300.0D);
        } catch (Exception var20) {
            var20.printStackTrace();
            System.exit(0);
        }

        return datacenter;
    }
    /**
     * Create NetworkCloudlet list with simple tasks
     * @param brokerId
     * @param cloudletsNumber
     * @return
     */
    public static List<NetworkCloudlet> createCloudletList(int brokerId, int cloudletsNumber) {
        List<NetworkCloudlet> list = new ArrayList<>();

        long fileSize = 300;
        long outputSize = 300;
        long seed = RandomConstants.CLOUDLET_UTILIZATION_SEED;
        UtilizationModel utilizationModelNull = new UtilizationModelNull();

        for (int i = 0; i < cloudletsNumber; i++) {
            NetworkCloudlet cloudlet = null;
            if (seed == -1) {
                cloudlet = new NetworkCloudlet(
                        i,
                        Constants.CLOUDLET_LENGTH,
                        Constants.CLOUDLET_PES,
                        fileSize,
                        outputSize,
                        1024,
                        utilizationModelNull,
                        utilizationModelNull,
                        new UtilizationModelStochastic());
            } else {
                cloudlet = new NetworkCloudlet(
                        i,
                        Constants.CLOUDLET_LENGTH,
                        Constants.CLOUDLET_PES,
                        fileSize,
                        outputSize,
                        1024,
                        new UtilizationModelStochastic(seed * i),
                        utilizationModelNull,
                        utilizationModelNull);
            }
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(i);
            cloudlet.stages.add(new TaskStage(NetworkConstants.EXECUTION, NetworkConstants.COMMUNICATION_LENGTH, 100, i, 1000, 0,cloudlet.getCloudletId()));
            list.add(cloudlet);
        }

        return list;
    }
    public static NetDatacenterBroker createBroker() throws Exception {
        return new NetDatacenterBroker("broker");
    }
}
