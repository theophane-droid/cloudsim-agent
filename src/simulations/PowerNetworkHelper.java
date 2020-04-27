package simulations;

import network.AgentHost;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.random.RandomConstants;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import power.LinearPowerSwitchModel;
import power.PowerAgentDatacenter;
import power.PowerAgentHost;
import power.PowerAgentSwitch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class wich Help to build simulations quickly
 * @author Théophane Dumas
 */
public class PowerNetworkHelper {

    /**
     * Create a NetworkDatacenter
     * @param hostList the datacenters host list
     * @param vmAllocationPolicy
     * @return the NetworkDatacenter created
     */
    public static PowerAgentDatacenter createDatacenter(
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

        PowerAgentDatacenter datacenter = null;

        try {
            Class datacenterClass = PowerAgentDatacenter.class;
            datacenter = (PowerAgentDatacenter) datacenterClass.getConstructor(
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
    public static void buildNetwork(int numhost, NetworkDatacenter dc) {

        int length = (int)Math.ceil(numhost/NetworkConstants.EdgeSwitchPort);
        PowerAgentSwitch agentSwitch[] = new PowerAgentSwitch[length];

        for (int i = 0; i < length; i++) {
            agentSwitch[i] = new PowerAgentSwitch("Edge" + i, NetworkConstants.EDGE_LEVEL, dc, new LinearPowerSwitchModel(100, 200, 24));
        }
        for(int i=0; i<length; i++){
            if(i<length-1) {
                System.out.println("1");
                agentSwitch[i].uplinkswitches = new ArrayList<>();
                agentSwitch[i].uplinkswitches.add(agentSwitch[i + 1]);
            }
            else{
                System.out.println("2");
                agentSwitch[i].uplinkswitches = new ArrayList<>();
                agentSwitch[i].uplinkswitches.add(agentSwitch[0]);
            }
            System.out.println("uplink : " + i + " => " + agentSwitch[i].uplinkswitches);
            dc.Switchlist.put(agentSwitch[i].getId(), agentSwitch[i]);
        }
        for (Host hs : dc.getHostList()) {
            NetworkHost hs1 = (NetworkHost) hs;
            hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
            int switchnum = (int) (hs.getId() / NetworkConstants.EdgeSwitchPort);
            agentSwitch[switchnum].hostlist.put(hs.getId(), hs1);
            dc.HostToSwitchid.put(hs.getId(), agentSwitch[switchnum].getId());
            hs1.sw = agentSwitch[switchnum];
            List<NetworkHost> hslist = hs1.sw.fintimelistHost.get(0D);
            if (hslist == null) {
                hslist = new ArrayList<NetworkHost>();
                hs1.sw.fintimelistHost.put(0D, hslist);
            }
            hslist.add(hs1);

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
    public static void printResults(NetworkDatacenter datacenter, List<NetworkVm> vmList, double lastClock, String experimentName, boolean outputCsv, String outputFolder) {
        double total = ((PowerAgentDatacenter)datacenter).getTotalUtilisation();
        System.out.println("total : " + total);
    }

    public static List<AgentHost> createHostList(int hostsNumber) {
        List<AgentHost> hostList = new ArrayList<>();
        for (int i = 0; i < hostsNumber; i++) {
            int hostType = i % Constants.HOST_TYPES;

            List<Pe> peList = new ArrayList<Pe>();
            for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
            }

            hostList.add(new PowerAgentHost(
                    i,
                    new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
                    new BwProvisionerSimple(Constants.HOST_BW),
                    Constants.HOST_STORAGE,
                    peList,
                    new VmSchedulerTimeSharedOverSubscription(peList),
                    new PowerModelLinear(300, 10)));
        }
        return hostList;
    }

    /**
     * Create NetworkVM list
     * @param brokerId
     * @param vmsNumber
     * @return
     */
    public static List<NetworkVm> createVmList(int brokerId, int vmsNumber){
        List<NetworkVm> vms = new ArrayList<>();
        for (int i = 0; i < vmsNumber; i++) {
            int vmType = i / (int) Math.ceil((double) vmsNumber / Constants.VM_TYPES);
            vms.add(new NetworkVm(
                    i,
                    brokerId,
                    Constants.VM_MIPS[vmType],
                    Constants.VM_PES[vmType],
                    Constants.VM_RAM[vmType],
                    Constants.VM_BW,
                    Constants.VM_SIZE,
                    "Xen",
                    new NetworkCloudletSpaceSharedScheduler()));
        }
        return vms;
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
