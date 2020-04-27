package power;

import network.AgentHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5670;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class allow to measure power consumption and to use network abilities
 * @author Théophane Dumas
 */
public class PowerAgentHost extends AgentHost implements PowerCalculator {
    List<Double> powerUtilisation;
    private PowerModel powerModel;
    double utilization = 0;
    public PowerAgentHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        powerUtilisation = new ArrayList<>();
        this.powerModel=powerModel;
    }

    @Override
    public List<Double> getPowerUtilisationHistory() {
        return powerUtilisation;
    }

    @Override
    public double getPowerSum() {
        double sum=0;
        for(double d:powerUtilisation)
            sum+=d;
        return sum;
    }

    @Override
    public double updateVmsProcessing(double currentTime) {
        double d = super.updateVmsProcessing(currentTime);
        updateUtilization();
        return d;
    }

    public void updateUtilization() {
        utilization =0.;
        for(Vm v: getVmList()){
            double sum = 0;
            for (Double m: v.getCurrentAllocatedMips())
                sum+=m;
            utilization+=sum;
        }
        utilization/=getTotalMips();
    }

    @Override
    public void updatePowerConsumption() {
        powerUtilisation.add(powerModel.getPower(utilization));
    }

    public double getUtilization() {
        return utilization;
    }

    public void setUtilization(double utilization) {
        this.utilization = utilization;
    }
}
