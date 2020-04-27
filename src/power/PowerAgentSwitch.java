package power;

import network.AgentSwitch;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

import java.util.ArrayList;
import java.util.List;

/**
 * A class wich allow to simulate a switch power consumption
 *
 * @author Théophane Dumas
 */
public class PowerAgentSwitch extends AgentSwitch implements PowerCalculator {
    private final PowerSwitchModel powerSwitchModel;
    protected List<Double> powerUtilisationList;

    /**
     * Constructor for AgentSwitch
     *
     * @param name
     * @param level
     * @param dc
     * @see EdgeSwitch#EdgeSwitch(String, int, NetworkDatacenter)
     */
    public PowerAgentSwitch(String name, int level, NetworkDatacenter dc, PowerSwitchModel powerSwitchModel) {
        super(name, level, dc, 24);
        powerUtilisationList = new ArrayList<>();
        this.powerSwitchModel = powerSwitchModel;
    }

    @Override
    public List<Double> getPowerUtilisation() {
        return powerUtilisationList;
    }

    @Override
    public double getPowerSum() {
        double sum = 0;
        for (Double d : powerUtilisationList)
            sum += d;
        return sum;
    }

    @Override
    public void updatePowerConsumption() {
        powerUtilisationList.add(this.powerSwitchModel.getPowerConsuption(this));
    }
}
