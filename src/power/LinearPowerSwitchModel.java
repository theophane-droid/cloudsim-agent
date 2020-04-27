package power;

/**
 * This class use a linear model to simulate a switch consommation.
 * It's based on a max,min, and the number of activate ports.
 *
 * @author Théophane Dumas
 */
public class LinearPowerSwitchModel extends PowerSwitchModel {
    int nbPorts;
    private double min, max;

    public LinearPowerSwitchModel(double min, double max, int nbPorts) {
        this.min = min;
        this.max = max;
        this.nbPorts = nbPorts;
    }

    @Override
    public double getPowerConsuption(PowerAgentSwitch powerAgentSwitch) {
        return min + (max - min) / nbPorts * (powerAgentSwitch.uplinkswitches.size() + powerAgentSwitch.hostlist.size());
    }
}
