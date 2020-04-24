package UnitTest;

import network.AgentSwitch;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.junit.Test;
import org.mockito.Mockito;
import simulations.NetworkHelper;

import java.util.ArrayList;

public class AgentSwitchUnitTest {
    @Test
    public void testUpdateConnexions() throws Exception {
        NetworkDatacenter dc = Mockito.mock(NetworkDatacenter.class);
        AgentSwitch agentSwitch = new AgentSwitch("agent", 0, dc);
    }
}
