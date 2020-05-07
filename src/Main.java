import org.cloudbus.cloudsim.Log;
import simulations.SimulationRunner;
import simulations.SimulationRunner1;

public class Main {
    public static void main(String[] args) {
        boolean enableOutput = true;
        boolean outputToFile = false;
        String outputFolder = "output";
        String workload = "20110303"; // PlanetLab workload

        SimulationRunner simulationRunner = new SimulationRunner1(
                "Simulation",
                workload,
                "res/planetlab",
                "output");
        try {
            simulationRunner.init();
            simulationRunner.start();
        } catch (Exception e) {
            Log.printLine("An exception occurs during the simulation !");
            e.printStackTrace();
        }
    }
}