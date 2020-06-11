package unittest;

import algorithms.Agent;
import network.AgentHost;
import network.AgentSwitch;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import network.Port;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

public class AgentUnitTest {

    @Test
    public void actionOnAgentSwitchTest1(){
        AgentSwitch agentSwitch = Mockito.mock(AgentSwitch.class);
        AgentHost h1 = Mockito.mock(AgentHost.class);
        AgentHost h2= Mockito.mock(AgentHost.class);
        Port p1 = new Port(false, h1);
        Port p2 = new Port(true, h2);
        List<Pair<Boolean, Port>> list = new ArrayList<>();
        list.add(new Pair(false, p1));
        list.add(new Pair(true, p2));
        Agent agent = new Agent(null);
        Mockito.when(agentSwitch.sortUsedAndUnusedConnexions()).thenReturn(list);
        Mockito.when(agentSwitch.isActive()).thenReturn(true);

        boolean b = agent.action(agentSwitch);

        Assert.assertFalse(b);
        Assert.assertEquals(p1.isOpen(), false);
        Assert.assertEquals(p2.isOpen(), true);
        Mockito.verify(agentSwitch, Mockito.times(1)).updateConnexions();
        Mockito.verify(agentSwitch, Mockito.times(1)).setIsActive(true);
    }
    @Test
    public void actionOnAgentSwitchTest2(){
        AgentSwitch agentSwitch = Mockito.mock(AgentSwitch.class);
        AgentHost h1 = Mockito.mock(AgentHost.class);
        AgentHost h2= Mockito.mock(AgentHost.class);
        Port p1 = new Port(false, h1);
        Port p2 = new Port(true, h2);
        List<Pair<Boolean, Port>> list = new ArrayList<>();
        list.add(new Pair(true, p1));
        list.add(new Pair(false, p2));
        Agent agent = new Agent(null);
        Mockito.when(agentSwitch.sortUsedAndUnusedConnexions()).thenReturn(list);
        Mockito.when(agentSwitch.isActive()).thenReturn(true);

        boolean b = agent.action(agentSwitch);

        Assert.assertTrue(b);
        Assert.assertEquals(p1.isOpen(), true);
        Assert.assertEquals(p2.isOpen(), false);
        Mockito.verify(agentSwitch, Mockito.times(1)).updateConnexions();
        Mockito.verify(agentSwitch, Mockito.times(1)).setIsActive(true);
    }
}
