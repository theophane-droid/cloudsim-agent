package unittest;

import network.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import power.AgentSwitchPowerModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AgentSwitchUnitTest {
    private NetworkDatacenter dc;
    private TestAgentSwitch agentSwitch;
    private Port port1;
    private Port port2;
    private AgentSwitch switch1;
    private AgentSwitch switch2;
    private AgentHost host1;
    private AgentHost host2;
    private TestAgentSwitch agentSwitch1;

/**
     * This test class will be usefull to access to the protected method processPacket
     */
    class TestAgentSwitch extends AgentSwitch {
        private boolean canCallSendRawPacket;

        public TestAgentSwitch(String name, NetworkDatacenter dc, boolean canCallSendRawPacket) {
            super(dc, 24, name, AgentSwitchPowerModel.CISCO_2960X);
            this.canCallSendRawPacket = canCallSendRawPacket;
        }

        public void processPacketPublic(List<RawPacket> packets) {
            processPackets(packets);
        }

        List<RawPacket> getpacketsRecieved() {
            return packetsRecieved;
        }

        List<RawPacket> getPacketToSort() {
            return packetsToSort;
        }

        @Override
        public void sendRawPaquet(RawPacket rawPacket) {
            if (!canCallSendRawPacket)
                throw new RuntimeException("It is forbidden in this test to call sendRawPacket");
            super.sendRawPaquet(rawPacket);
        }
    }
    @Before
    public void setupTest() {
        CloudSim.init(0, Calendar.getInstance(), false);
        dc = Mockito.mock(NetworkDatacenter.class);
        agentSwitch = new TestAgentSwitch("agent", dc, false);
        agentSwitch1 = new TestAgentSwitch("agent", dc, true);
        port1 = Mockito.mock(Port.class);
        Mockito.when(port1.isOpen()).thenReturn(true);
        port2 = Mockito.mock(Port.class);
        Mockito.when(port2.isOpen()).thenReturn(false);
        agentSwitch.getUpSwitchConnexions().add(port2);
        host1 = Mockito.mock(AgentHost.class);
        host2 = Mockito.mock(AgentHost.class);
        Mockito.when(host1.getId()).thenReturn(0);
        Mockito.when(host1.getId()).thenReturn(1);
        switch1 = Mockito.mock(AgentSwitch.class);
        switch2 = Mockito.mock(AgentSwitch.class);
    }
    @Test
    public void processPacketTTLOvertest() {
        List<RawPacket> packets = new ArrayList<>();
        // * RawPacket don't do any traitement so does'nt need to be mocked
        RawPacket packet = new RawPacket(-1, -1, null, null, null);
        while (packet.getTTL() > 0)
            packet.decrementTTL();
        packets.add(packet);
        agentSwitch.processPacketPublic(packets);

        Assert.assertEquals(agentSwitch.getPacketToSort().size(), 0);
        Assert.assertEquals(agentSwitch.getpacketsRecieved().size(), 0);
    }

    @Test
    public void testUpdateConnexionsUpLinkFalse() throws Exception {
        System.out.println("sw 1 : " + switch1 + " sw 2 : " + switch2);
        agentSwitch.getUpSwitchConnexions().add(port1);
        agentSwitch.getUpSwitchConnexions().add(port2);
        Mockito.when(port1.getReliedObject()).thenReturn(switch1);
        Mockito.when(port2.getReliedObject()).thenReturn(switch2);
        agentSwitch.updateConnexions();

        // * the only open port is the first one, so uplinkswitches should contain only host1
        List<AgentSwitch> shouldBeEqualTo = new ArrayList();
        shouldBeEqualTo.add(switch2);
        Assert.assertNotEquals(new List[]{shouldBeEqualTo}, new List[]{agentSwitch.uplinkswitches});
    }
    @Test
    public void testUpdateConnexionsUpLinkTrue() {
        agentSwitch.getUpSwitchConnexions().add(port1);
        agentSwitch.getUpSwitchConnexions().add(port2);
        Mockito.when(port1.getReliedObject()).thenReturn(switch1);
        Mockito.when(port2.getReliedObject()).thenReturn(switch2);
        agentSwitch.updateConnexions();

        // * the only open port is the first one, so uplinkswitches should contain only host1
        List<AgentSwitch> shouldBeEqualTo = new ArrayList();
        shouldBeEqualTo.add(switch1);
        Assert.assertArrayEquals(new List[]{shouldBeEqualTo}, new List[]{agentSwitch.uplinkswitches});
    }


    @Test
    public void processPacketDestinatedToSwitch() {
        List<RawPacket> packets = new ArrayList<>();
        // * RawPacket don't do any traitement so does'nt need to be mocked
        RawPacket packet = new RawPacket(-1, agentSwitch.getId(), null, TestAgentSwitch.class, null);
        packets.add(packet);
        agentSwitch.processPacketPublic(packets);

        Assert.assertEquals(agentSwitch.getPacketToSort().size(), 0);
        Assert.assertEquals(agentSwitch.getpacketsRecieved().size(), 1);
        Assert.assertEquals(agentSwitch.getpacketsRecieved().get(0), packet);
    }
    @Test
    public void processPacketUp(){
        List<RawPacket> packets = new ArrayList<>();
        // * RawPacket don't do any traitement so does'nt need to be mocked
        RawPacket packet = new RawPacket(-1, agentSwitch.getId(), null, null, null);
        packets.add(packet);
        agentSwitch1.uplinkswitches.add(agentSwitch);
        agentSwitch1.processPacketPublic(packets);

        Assert.assertEquals(0, agentSwitch.getPacketToSort().size());
        Assert.assertEquals(0, agentSwitch.getpacketsRecieved().size());
    }

    @Test
    public void switchDeactivatedTest() {
        agentSwitch.setIsActive(false);
        List<RawPacket> packets = new ArrayList<>();
        RawPacket packet = new RawPacket(-1, agentSwitch.getId(), null, agentSwitch.getClass(), null);
        packets.add(packet);
        agentSwitch.processPacketPublic(packets);
        Assert.assertEquals(0, agentSwitch.getpacketsRecieved().size());
        Assert.assertEquals(0, agentSwitch.getPacketToSort().size());
        agentSwitch.setIsActive(true);
    }
    @Test
    public void packetToBeSentToANeighboringHost(){
        agentSwitch1.getHostConnexions().add(new Port(true, host1));
        agentSwitch1.getHostConnexions().add(new Port(true, host2));
        agentSwitch1.updateConnexions();
        List<RawPacket> packets = new ArrayList<>();
        List<RawPacket> mockedToSortList = Mockito.mock(List.class);
        RawPacket packet = new RawPacket(-1, host1.getId(), null, host1.getClass(), null);
        Mockito.when(host1.getPacketsToSort()).thenReturn(mockedToSortList);
        packets.add(packet);
        agentSwitch1.processPacketPublic(packets);

        Assert.assertEquals(0, agentSwitch1.getpacketsRecieved().size());
        Assert.assertEquals(0, agentSwitch1.getPacketToSort().size());
        Mockito.verify(host1, Mockito.times(1)).getPacketsToSort();
        Mockito.verify(mockedToSortList, Mockito.times(1)).add(packet);

    }
}
