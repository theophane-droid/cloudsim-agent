import org.cloudbus.cloudsim.Log;
import simulations.*;
import org.ini4j.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static java.lang.System.exit;

public class Main {
    /**
     * Nothing to change here, please edit the simulation.ini file
     * @param args
     */
    public static void main(String[] args) {
        Wini ini = null;
        try {
           ini = new Wini(new File("res/simulation.ini"));
        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
        SimulationRunner simulationRunner = null;
        try {
            Class simulationRunnerClass = Class.forName("simulations." + ini.get("agent","detection_method", String.class) + "Simulation");
            simulationRunner = (SimulationRunner)simulationRunnerClass.getConstructor(ini.getClass()).newInstance(ini);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            exit(-1);
        }
        try {
            simulationRunner.init();
            simulationRunner.start();
        } catch (Exception e) {
            Log.printLine("An exception occurs during the simulation !");
            e.printStackTrace();
        }
    }
}