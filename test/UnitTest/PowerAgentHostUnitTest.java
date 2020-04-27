package UnitTest;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import power.PowerAgentHost;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PowerAgentHostUnitTest {
    private PowerAgentHost powerAgentHost;
    private PowerModel model;

    /**
     * This class has been created to be a tool in the following test
     */
    class PowerTestModel implements PowerModel {

        @Override
        public double getPower(double v) throws IllegalArgumentException {
            return v;
        }
    }
    @Before
    public void setupTest(){
        CloudSim.init(0, Calendar.getInstance(), false);
        List<Pe> listPe=new ArrayList<>();
        for(int i=0; i<4; i++){
            listPe.add(new Pe(i, new PeProvisionerSimple(1000)));
        }
        // * we suppose that powerLinearModel works
        model = new PowerTestModel();
        powerAgentHost = new PowerAgentHost(0,
                new RamProvisionerSimple(1000),
                new BwProvisionerSimple(10000),
                1000,
                listPe,
                new VmSchedulerSpaceShared(listPe),
                model
                );
    }
    @Test
    public void testUpdatePowerConsumption(){
        powerAgentHost.getVmList().clear();
        List<Double> l1 = new ArrayList();
        List<Double> l2 = new ArrayList();
        List<Double> l3 = new ArrayList();
        List<Double> l4 = new ArrayList();
        l1.add(100.);
        l2.add(30.);
        l3.add(50.);
        l4.add(250.);
        Vm vm1 = Mockito.mock(Vm.class);
        Vm vm2 = Mockito.mock(Vm.class);
        Mockito.when(vm1.getCurrentAllocatedMips()).thenReturn(l1, l2);
        Mockito.when(vm2.getCurrentAllocatedMips()).thenReturn(l3, l4);
        powerAgentHost.getVmList().add(vm1);
        powerAgentHost.getVmList().add(vm2);
        powerAgentHost.updateUtilization();
        powerAgentHost.updatePowerConsumption();
        powerAgentHost.updateUtilization();
        powerAgentHost.updatePowerConsumption();

        double utilizationExpected1 = (100.+50.)/(1000*4);
        double utilizationExpected2 = (30.+250.)/(1000*4);
        List<Double> expected = new ArrayList<>();
        expected.add(model.getPower(utilizationExpected1));
        expected.add(model.getPower(utilizationExpected2));

        Assert.assertEquals(expected, powerAgentHost.getPowerUtilisationHistory());
    }
}