package algorithms;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * This abstract class allow to select a vm to migrate
 * (it works like PowerVmSelectionPolicy but it takes not only PowerHost)
 * @author Théophane Dumas
 */
public interface VmSelectionPolicy {
    public Vm selectVm(Host host);
}