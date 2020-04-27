package UnitTest;

import network.AgentHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.NetDatacenterBroker;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkVm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import power.PowerAgentSwitch;
import power.PowerSwitchModel;
import simulations.NetworkHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PowerAgentSwitchUnitTest {
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
    public void testUpdatePowerConsumption() {
        PowerSwitchModel powerSwitchModel = Mockito.mock(PowerSwitchModel.class);
        PowerAgentSwitch powerAgentSwitch = new PowerAgentSwitch("switch", -1, networkDatacenter, powerSwitchModel);
        Mockito.when(powerSwitchModel.getPowerConsuption(Mockito.any())).thenReturn(12., 1., 3., -1.);
        powerAgentSwitch.updatePowerConsumption();
        powerAgentSwitch.updatePowerConsumption();
        powerAgentSwitch.updatePowerConsumption();
        powerAgentSwitch.updatePowerConsumption();

        ArrayList<Double> shouldBeEqualTo = new ArrayList<>();
        shouldBeEqualTo.add(12.);
        shouldBeEqualTo.add(1.);
        shouldBeEqualTo.add(3.);
        shouldBeEqualTo.add(-1.);

        Assert.assertEquals(shouldBeEqualTo, powerAgentSwitch.getPowerUtilisationHistory());
    }

    @Test
    public void testSum() {
        PowerSwitchModel powerSwitchModel = Mockito.mock(PowerSwitchModel.class);
        PowerAgentSwitch powerAgentSwitch = new PowerAgentSwitch("switch", -1, networkDatacenter, powerSwitchModel);
        Mockito.when(powerSwitchModel.getPowerConsuption(Mockito.any())).thenReturn(12., 1., 3., -1.);
        powerAgentSwitch.updatePowerConsumption();
        powerAgentSwitch.updatePowerConsumption();
        powerAgentSwitch.updatePowerConsumption();
        powerAgentSwitch.updatePowerConsumption();

        Assert.assertTrue(12. + 1. + 3. + -1 == powerAgentSwitch.getPowerSum());
    }
}
