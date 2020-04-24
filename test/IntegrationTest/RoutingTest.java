package IntegrationTest;

import network.AgentHost;
import network.AgentSwitch;
import network.RawPacket;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import simulations.NetworkHelper;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class RoutingTest {
    /**
     * A class wich test that the rawpacket routing works
     * @author Théophane Dumas
     */
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
        List <NetworkCloudlet> cloudletList = NetworkHelper.createCloudletList(broker.getId(), 10);
        networkDatacenter = NetworkHelper.createDatacenter(hostList, new VmAllocationPolicySimple(hostList));
        NetworkHelper.buildNetwork(10, networkDatacenter);
        broker.setLinkDC(networkDatacenter);
        broker.submitCloudletList(cloudletList);
        broker.submitVmList(vmLists);
    }
    @Test
    public void testSendPacketToSwitch() throws Exception {
        // * define the switch network packet
        Iterator<Integer> it = networkDatacenter.getEdgeSwitch().keySet().iterator();
        it.next();
        int idDest = it.next();
        AgentSwitch switchDest = (AgentSwitch) networkDatacenter.getEdgeSwitch().get(idDest);
        RawPacket packet = Mockito.mock(RawPacket.class);
        Mockito.when(packet.getClassDest()).thenReturn(switchDest.getClass());
        Mockito.when(packet.getIdDest()).thenReturn(switchDest.getId());
        ((AgentHost)networkDatacenter.getHostList().get(0)).sendRawPaquet(packet);

        CloudSim.terminateSimulation(1000);
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        // * we check that the rawpaquet has been received by switchDest
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(switchDest);
        // * we check that the rawpaquet has been used just one
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(Mockito.anyObject());
    }
    @Test
    public void testSendPacketToHost() throws Exception {
        AgentHost hostDest = (AgentHost) networkDatacenter.getHostList().get(9);
        System.out.println("hostDest : " + hostDest);
        RawPacket packet = Mockito.mock(RawPacket.class);
        Mockito.when(packet.getClassDest()).thenReturn(hostDest.getClass());
        Mockito.when(packet.getIdDest()).thenReturn(hostDest.getId());
        ((AgentHost)networkDatacenter.getHostList().get(0)).sendRawPaquet(packet);

        CloudSim.terminateSimulation(1000);
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        // * we check that the rawpaquet has been received by switchDest
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(hostDest);
        // * we check that the rawpaquet has been used just one
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(Mockito.anyObject());
    }
}
