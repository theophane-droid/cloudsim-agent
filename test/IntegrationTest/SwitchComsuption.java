package IntegrationTest;

import network.AgentHost;
import network.Port;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import power.LinearPowerSwitchModel;
import power.PowerAgentSwitch;
import power.PowerSwitchModel;
import simulations.NetworkHelper;

import java.util.Calendar;
import java.util.List;

public class SwitchComsuption {
    private NetworkDatacenter networkDatacenter;

    @Before
    public void initSimulation() throws Exception {
        Log.setDisabled(true);
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(10);
        System.out.println(hostList);
        NetDatacenterBroker broker = NetworkHelper.createBroker();
        List<NetworkVm> vmLists = NetworkHelper.createVmList(broker.getId(), 20);
        List<NetworkCloudlet> cloudletList = NetworkHelper.createCloudletList(broker.getId(), 10);
        networkDatacenter = NetworkHelper.createDatacenter(hostList, new VmAllocationPolicySimple(hostList));
        NetworkHelper.buildNetwork(10, networkDatacenter);
        broker.setLinkDC(networkDatacenter);
        broker.submitCloudletList(cloudletList);
        broker.submitVmList(vmLists);
    }

    @Test
    public void testLinearConsumption() {
        PowerSwitchModel linearModel = new LinearPowerSwitchModel(100, 200, 24);
        PowerAgentSwitch powerAgentSwitch = new PowerAgentSwitch("switch", -1, networkDatacenter, linearModel);
        // * with 0 up ports
        powerAgentSwitch.updatePowerConsumption();
        Assert.assertTrue(powerAgentSwitch.getPowerUtilisationHistory().get(0) == 100);

        // * with 12 up ports
        for (int i = 0; i < 12; i++) {
            NetworkHost h = Mockito.mock(NetworkHost.class);
            Mockito.when(h.getId()).thenReturn(i);
            powerAgentSwitch.getHostConnexions().add(new Port(true, h));
        }
        powerAgentSwitch.updateConnexions();
        powerAgentSwitch.updatePowerConsumption();
        System.out.println();
        Assert.assertTrue(powerAgentSwitch.getPowerUtilisationHistory().get(1) == 150);

        // * with 24 up ports
        for (int i = 12; i < 24; i++) {
            NetworkHost h = Mockito.mock(NetworkHost.class);
            Mockito.when(h.getId()).thenReturn(i);
            powerAgentSwitch.getHostConnexions().add(new Port(true, h));
        }
        powerAgentSwitch.updateConnexions();
        powerAgentSwitch.updatePowerConsumption();
        System.out.println();
        Assert.assertTrue(powerAgentSwitch.getPowerUtilisationHistory().get(2) == 200);
    }
}
