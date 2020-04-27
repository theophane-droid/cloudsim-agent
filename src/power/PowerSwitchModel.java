package power;

/**
 * This abstract class can be used to modelise the consommation of a switch
 *
 * @author Théophane Dumas
 */
public abstract class PowerSwitchModel {
    public abstract double getPowerConsuption(PowerAgentSwitch powerAgentSwitch);
}
