import jdk.jshell.execution.Util;
import org.cloudbus.cloudsim.Log;
import simulations.*;
import org.ini4j.*;
import utils.Utils;
import utils.Vars;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;
import static java.lang.System.out;

public class Main {
    /**
     * Nothing to change here, please edit the simulation.ini file
     * @param args
     */
    public static void main(String[] args) {
        Wini simulationIni = null;
        Wini automaticsIni = null;
        try {
            simulationIni = new Wini(new File("res/simulation.ini"));
            automaticsIni = new Wini(new File("res/automatics.ini"));
        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
        System.out.println("allox : " + automaticsIni.get("globals","allow_automatic_run", Boolean.TYPE));
        if(automaticsIni.get("globals","allow_automatic_run", Boolean.TYPE))
            runAutomatics(simulationIni, automaticsIni);
        else
            runSimulationFromIni(simulationIni);
    }
    public static Map<String, Double> runSimulationFromIni(Wini ini){
        Vars.loadFromIniFile(ini);
        SimulationRunner simulationRunner = null;
        try {
            Class simulationRunnerClass = Class.forName("simulations." + ini.get("agent","detection_method", String.class) + "Simulation");
            simulationRunner = (SimulationRunner)simulationRunnerClass.getConstructor(ini.getClass()).newInstance(ini);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            exit(-1);
        }
        Map<String, Double> result = null;
        try {
            simulationRunner.init();
            result = simulationRunner.start();
        } catch (Exception e) {
            Log.printLine("An exception occurs during the simulation !");
            e.printStackTrace();
        }
        return result;
    }
    public static void runAutomatics(Wini iniSimulation, Wini iniAutomatics){
        String varName = iniAutomatics.get("globals","parameter_to_modify_name", String.class);
        String varSection = iniAutomatics.get("globals","parameter_to_modify_section", String.class);
        String[] tmp = iniAutomatics.get("globals","successive_values", String.class).split(",");
        double[] values = new double[tmp.length];
        for(int i=0; i<tmp.length; i++){
            values[i] = Double.parseDouble(tmp[i]);
        }
        List<Map<String, Double>> resultList = new ArrayList<>();
        for(double d: values){
            if((double)Math.round(d)==d)
                iniSimulation.put(varSection, varName, (int)d);
            else
                iniSimulation.put(varSection, varName, d);
            Map<String, Double> result = runSimulationFromIni(iniSimulation);
            result.put("var", d);
            resultList.add(result);
        }
        String outPath = iniAutomatics.get("out", "path_to_out_file", String.class);
        boolean b = Utils.writeResult(outPath, resultList, varName);
        if(b){
            System.out.println("\n\nresultat écrits dans le fichier " + outPath);
        }
        else{
            System.out.println("\n\nerreur écriture dans le fichier " + outPath);
        }
    }
}