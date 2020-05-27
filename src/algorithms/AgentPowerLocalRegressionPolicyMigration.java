package algorithms;

import network.AgentHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.*;
import utils.Vars;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class makes easier to create the policy and correct a bug in CloudSim (using this class)
 * @author Théophane
 */
public class AgentPowerLocalRegressionPolicyMigration extends PowerVmAllocationPolicyMigrationLocalRegression {

    AgentPowerLocalRegressionPolicyMigration(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy, double safetyParameter, double schedulingInterval, PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
        super(hostList, vmSelectionPolicy, safetyParameter, schedulingInterval, fallbackVmAllocationPolicy);
    }

    public static AgentPowerLocalRegressionPolicyMigration createAgentPolicy(List<AgentHost> hostList){
        PowerVmSelectionPolicy selectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
        PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
                hostList,
                selectionPolicy,
                0.7);
        return new AgentPowerLocalRegressionPolicyMigration(hostList, new PowerVmSelectionPolicyMinimumMigrationTime(), Vars.SAFETY_PARAMETER, Vars.POWER_MEASURE_INTERVAL, fallbackVmSelectionPolicy);
    }

    /**
     * We Override this method to correct a bug in CloudSim
     * @param host
     * @param vm
     * @return
     */
    @Override
    protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
         double power = 0.0D;
         power = host.getPowerModel().getPower(this.getMaxUtilizationAfterAllocation(host, vm));
         return power;
    }

    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        double minPower = 1.7976931348623157E308D;
        PowerHost allocatedHost = null;
        Iterator i$ = this.getHostList().iterator();

        while(true) {
            PowerHost host;
            do {
                if (!i$.hasNext()) {
                    return allocatedHost;
                }

                host = (PowerHost)i$.next();
            } while(!host.isSuitableForVm(vm) || this.isHostOverUtilizedAfterAllocation(host, vm));

            try {
                double powerAfterAllocation = this.getPowerAfterAllocation(host, vm);
                if (powerAfterAllocation != -1.0D) {
                    double powerDiff = powerAfterAllocation - host.getPower();
                    if (powerDiff < minPower) {
                        minPower = powerDiff;
                        allocatedHost = host;
                    }
                }
            } catch (Exception var12) {
            }
        }
    }
}
