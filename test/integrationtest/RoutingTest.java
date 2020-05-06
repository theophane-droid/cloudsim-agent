package integrationtest;

import algorithms.Agent;
import network.AgentDatacenter;
import network.AgentHost;
import network.AgentSwitch;
import network.RawPacket;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkVm;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumMigrationTime;
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
    private AgentDatacenter agentDatacenter;

    @Before
    public void initSimulation() throws Exception {
        Log.setDisabled(true);
        // * define the simulation
        CloudSim.init(0, Calendar.getInstance(), false);
        List<AgentHost> hostList = NetworkHelper.createHostList(10);
        System.out.println(hostList);
        DatacenterBroker broker = Helper.createBroker();
        List<Vm> vmLists = Helper.createVmList(broker.getId(), 20);
        List <NetworkCloudlet> cloudletList = NetworkHelper.createCloudletList(broker.getId(), 10);
        agentDatacenter = NetworkHelper.createDatacenter("datacenter0", hostList, new PowerVmAllocationPolicyMigrationStaticThreshold(hostList, new PowerVmSelectionPolicyMinimumMigrationTime(), 0.7D));
        NetworkHelper.buildNetwork(10, agentDatacenter);

        broker.submitCloudletList(cloudletList);
        broker.submitVmList(vmLists);
    }
    @Test
    public void testSendPacketToSwitch() {
        // * define the switch network packet
        Iterator<Integer> it = agentDatacenter.getAgentSwitchs().keySet().iterator();
        it.next();
        int idDest = it.next();
        AgentSwitch switchDest = (AgentSwitch) agentDatacenter.getAgentSwitchs().get(idDest);
        RawPacket packet = Mockito.mock(RawPacket.class);
        Mockito.when(packet.getClassDest()).thenReturn(switchDest.getClass());
        Mockito.when(packet.getIdDest()).thenReturn(switchDest.getId());
        Mockito.when(packet.getTTL()).thenReturn(1);
        ((AgentHost) agentDatacenter.getHostList().get(0)).sendRawPaquet(packet);

        CloudSim.terminateSimulation(1000);
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        // * we check that the rawpaquet has been received by switchDest
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(switchDest);
        // * we check that the rawpaquet has been used just one
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(Mockito.anyObject());
    }
    @Test
    public void testSendPacketToHost() {
        AgentHost hostDest = (AgentHost) agentDatacenter.getHostList().get(9);
        System.out.println("hostDest : " + hostDest);
        RawPacket packet = Mockito.mock(RawPacket.class);
        Mockito.when(packet.getClassDest()).thenReturn(hostDest.getClass());
        Mockito.when(packet.getIdDest()).thenReturn(hostDest.getId());
        Mockito.when(packet.getTTL()).thenReturn(1);
        ((AgentHost) agentDatacenter.getHostList().get(0)).sendRawPaquet(packet);

        CloudSim.terminateSimulation(1000);
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        // * we check that the rawpaquet has been received by switchDest
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(hostDest);
        // * we check that the rawpaquet has been used just one
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(Mockito.anyObject());
    }
    @Test
    public void testSendAgentToHost() {
        AgentHost hostDest = (AgentHost) agentDatacenter.getHostList().get(9);
        System.out.println("hostDest : " + hostDest);
        Agent agent  = Mockito.mock(Agent.class);
        RawPacket packet = Mockito.mock(RawPacket.class);
        Mockito.when(packet.getClassDest()).thenReturn(hostDest.getClass());
        Mockito.when(packet.getIdDest()).thenReturn(hostDest.getId());
        Mockito.when(packet.getTTL()).thenReturn(1);
        Mockito.when(packet.getContent()).thenReturn(agent);
        ((AgentHost) agentDatacenter.getHostList().get(0)).sendRawPaquet(packet);

        CloudSim.terminateSimulation(1000);
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        // * we check that the rawpaquet has been received by switchDest
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(hostDest);
        // * we check that the rawpaquet has been used just one
        Mockito.verify(packet, Mockito.times(1)).setRecievedBy(Mockito.anyObject());
        // * we check that the agent action method has been called
        Mockito.verify(agent, Mockito.times(1)).action(hostDest);
    }
}
