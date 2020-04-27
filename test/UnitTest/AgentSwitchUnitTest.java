package UnitTest;

import network.AgentSwitch;
import network.Port;
import network.RawPacket;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.Switch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import simulations.NetworkHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class AgentSwitchUnitTest {
    private NetworkDatacenter dc;
    private TestAgentSwitch agentSwitch;
    private Port port1;
    private Port port2;
    private Switch switch1;
    private Switch switch2;
    private Host host1;
    private Host host2;
    private TestAgentSwitch agentSwitch1;

    /**
     * This test class will be usefull to access to the protected method processPacket
     */
    class TestAgentSwitch extends AgentSwitch{
        private boolean canCallSendRawPacket;
        public TestAgentSwitch(String name, int level, NetworkDatacenter dc, boolean canCallSendRawPacket) {
            super(name, level, dc);
            this.canCallSendRawPacket = canCallSendRawPacket;
        }
        public void processPacketPublic(List<RawPacket> packets){
            processPackets(packets);
        }
        List<RawPacket> getpacketsRecieved(){
            return packetsRecieved;
        }
        List<RawPacket> getPacketToSort(){
            return packetsToSort;
        }
        @Override
        public void sendRawPaquet(RawPacket rawPacket){
            if(!canCallSendRawPacket)
                throw new RuntimeException("It is forbidden in this test to call sendRawPacket");
            super.sendRawPaquet(rawPacket);
        }
    }

    @Before
    public void setupTest() {
        CloudSim.init(0, Calendar.getInstance(), false);
        dc = Mockito.mock(NetworkDatacenter.class);
        agentSwitch = new TestAgentSwitch("agent", 0, dc, false);
        agentSwitch1 = new TestAgentSwitch("agent", 0, dc, true);
        port1 = Mockito.mock(Port.class);
        Mockito.when(port1.isOpen()).thenReturn(true);
        port2 = Mockito.mock(Port.class);
        Mockito.when(port2.isOpen()).thenReturn(false);
        agentSwitch.getUpSwitchConnexions().add(new Port());
        host1 = Mockito.mock(Host.class);
        host2 = Mockito.mock(Host.class);
        switch1 = Mockito.mock(Switch.class);
        switch2 = Mockito.mock(Switch.class);
    }
    @Test
    public void testUpdateConnexionsUpLinkFalse() throws Exception {
        agentSwitch.getUpSwitchConnexions().add(port1);
        agentSwitch.getUpSwitchConnexions().add(port2);
        Mockito.when(port1.getReliedObject()).thenReturn(switch1);
        Mockito.when(port2.getReliedObject()).thenReturn(switch2);
        agentSwitch.updateConnexions();

        // * the only open port is the first one, so uplinkswitches should contain only host1
        List<Switch> shouldBeEqualTo = new ArrayList();
        shouldBeEqualTo.add(switch2);
        Assert.assertNotEquals(new List[]{shouldBeEqualTo}, new List[]{agentSwitch.uplinkswitches});
    }
    @Test
    public void testUpdateConnexionsUpLinkTrue() throws Exception {
        agentSwitch.getUpSwitchConnexions().add(port1);
        agentSwitch.getUpSwitchConnexions().add(port2);
        Mockito.when(port1.getReliedObject()).thenReturn(switch1);
        Mockito.when(port2.getReliedObject()).thenReturn(switch2);
        agentSwitch.updateConnexions();

        // * the only open port is the first one, so uplinkswitches should contain only host1
        List<Switch> shouldBeEqualTo = new ArrayList();
        shouldBeEqualTo.add(switch1);
        Assert.assertArrayEquals(new List[]{shouldBeEqualTo}, new List[]{agentSwitch.uplinkswitches});
    }
    @Test
    public void processPacketTTLOvertest(){
        List<RawPacket> packets = new ArrayList<>();
        // * RawPacket don't do any traitement so does'nt need to be mocked
        RawPacket packet = new RawPacket(-1,-1, null, null, null);
        packet.ttl=0;
        packets.add(packet);
        agentSwitch.processPacketPublic(packets);

        Assert.assertEquals(agentSwitch.getPacketToSort().size(), 0);
        Assert.assertEquals(agentSwitch.getpacketsRecieved().size(), 0);
    }
    @Test
    public void processPacketDestinatedToSwitch(){
        List<RawPacket> packets = new ArrayList<>();
        // * RawPacket don't do any traitement so does'nt need to be mocked
        RawPacket packet = new RawPacket(-1,agentSwitch.getId(), null, TestAgentSwitch.class, null);
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
        RawPacket packet = new RawPacket(-1,agentSwitch.getId(), null, null, null);
        packets.add(packet);
        packet.ttl=50;
        agentSwitch1.uplinkswitches.add(agentSwitch);
        agentSwitch1.processPacketPublic(packets);

        Assert.assertEquals(agentSwitch.getPacketToSort().size(), 0);
        Assert.assertEquals(agentSwitch.getpacketsRecieved().size(), 0);
    }
}