package algorithms;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * This class will select the vm to migrate using the Minimum Migration Time policy
 * @author Théophane Dumas
 */
public class VmSelectionPolicyMinimumMigrationTime implements VmSelectionPolicy {
    @Override
    public Vm selectVm(Host host) {
        double min=Double.MAX_VALUE;
        Vm minVm = host.getVmList().get(0);
        for(Vm vm: host.getVmList()){
            if(vm.getRam()<min){
                min=vm.getRam();
                minVm=vm;
            }
        }
        return minVm;
    }
}
