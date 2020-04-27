package power;

import java.util.List;

/**
 * An interface wich can be used the create object in wich the power is calculated
 *
 * @author Théophane Dumas
 */
public interface PowerCalculator {
    public List<Double> getPowerUtilisationHistory();

    public double getPowerSum();

    public void updatePowerConsumption();
}
