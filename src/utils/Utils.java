package utils;

import algorithms.Agent;
import com.opencsv.CSVWriter;
import network.AgentDatacenter;
import network.AgentHost;
import network.AgentSwitch;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.ini4j.Wini;
import simulations.NetworkHelper;
import network.Port;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
    public static List copyList(List listToCopy){
        List l = new ArrayList();
        for(Object o: listToCopy){
            l.add(o);
        }
        return l;
    }
    /**
     * This method calculate approximately the total power consumption
     * @param powerConsumptionHistory the history of an AgentHost or an AgentSwitch
     * @return total consumption
     */
    public static double calcPowerConsumtion(List<Pair<Double, Double>> powerConsumptionHistory){
        double sum=0;
        Pair<Double, Double> lastPair, actualPair;
        // * to get the total power consumption we do a simple linear interpolation
        for(int i=1; i<powerConsumptionHistory.size(); i++){
            lastPair = powerConsumptionHistory.get(i-1);
            actualPair = powerConsumptionHistory.get(i);
            sum += actualPair.getSecond() * (actualPair.getFirst()-lastPair.getFirst());
        }
        return sum;
    }

    /**
     * This method print informations about datacenter in parameters
     * @param dc
     * @param clock
     */
    public static void printDatacenterState(AgentDatacenter dc, double clock){
        System.out.println(dc.getName() + " at " + clock);
        System.out.println("==========Hosts==========");
        for(Host h1: dc.getHostList()){
            AgentHost  h = (AgentHost) h1;
            System.out.println("    host " + h.getId() + " : " + "( is active : " + h.isUp() + " )" + ", utilization = " + h.getUtilizationOfCpu());
            for(Vm v: h.getVmList()){
                System.out.println("        vm " + v.getId());
            }
        }
        System.out.println("===========Switchs==========");
        for(int i : dc.getAgentSwitchs().keySet()){
            AgentSwitch a = dc.getAgentSwitchs().get(i);
            System.out.println("    switch " + a.getName() + " ( is active : " + a.isActive() + " ) id = " + a.getId());
        }
    }

    /**
     * @param list the related list
     * @return the mean of a set of value
     */
    public static long mean1(List<Long> list){
        long mean=0;
        for(Long l: list)
            mean+=l;
        return mean/list.size();
    }
    public static double mean2(List<Double> list){
        double mean=0;
        for(Double l: list)
            mean+=l;
        return (long) (mean/list.size());
    }
    public static double stdDeviation(List<Double> list){
        long mean = (long) mean2(list);
        double variance = 0;
        for(int i=0; i<list.size(); i++){
            variance+=Math.pow(list.get(i)-mean,2);
        }
        return variance/list.size();
    }
    /**
     * This method use the reverse normal law, to generate a set of value
     * @param mean
     * @param standardDeviation
     * @param  numberOfValues
     * @return the list of values
     */
    public static List<Long> generateRandomizedValues(double mean, double standardDeviation, int numberOfValues){
        List<Long> result = new ArrayList<>();
        List<Double> result0 = new ArrayList<>();
        for(int i=0; i<numberOfValues; i++){
            result0.add(Math.cos(14*i));
        }
        double var0 = stdDeviation(result0);
        double A = standardDeviation/var0;
        for(int i=0; i<numberOfValues; i++){
            result.add((long) (A * result0.get(i)));
        }
        long moy0 = mean1(result);
        double B = mean - moy0;
        for(int i=0; i<numberOfValues; i++){
            result.set(i, (long) (B+result.get(i)));
        }
        return result;
    }

    public static List<Cloudlet> createTheProperCloudletList(int brokerId, int nbCloudlets, List<Vm> vmList,  Wini ini){
        List<Cloudlet> list;
        boolean isRandomized = ini.get("cloudlets", "randomize_cloudlet_length", Boolean.TYPE);
        long mean = ini.get("cloudlets","mean_cloudlet_length", Long.TYPE);
        long stdDeviation = ini.get("cloudlets","standard_cloudlet_deviation", Long.TYPE);
        if(isRandomized)
            list = NetworkHelper.createRandomizedCloudletList(brokerId, nbCloudlets, vmList, mean, stdDeviation);
        else
            list = NetworkHelper.createCloudletList(brokerId, nbCloudlets, vmList, mean);
        return list;
    }

    public static boolean writeResult(String outPath, List<Map<String, Double>> resultList, String variablesName) {
        String[] headers = new String[]{variablesName, "power consumption (kWh)"};
        CSVWriter writer;
        try {
            writer = new CSVWriter(new FileWriter(new File(outPath)));

        } catch (IOException e) {
            return false;
        }
        writer.writeNext(headers);
        String[] line;
        for(Map<String, Double> current : resultList){
            double power = current.get("hosts_power") + current.get("switchs_power");
            power/=(3600*1000);
            double varValue = current.get("var");
            line = new String[]{Double.toString(varValue), Double.toString(power)};
            writer.writeNext(line);
        }
        try {
            writer.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public static void addLink(AgentSwitch upSwitch, AgentSwitch downSwitch){
        upSwitch.getDownSwitchConnexions().add(new Port(true, downSwitch));
        downSwitch.getUpSwitchConnexions().add(new Port(true, upSwitch));
        upSwitch.updateConnexions();
        downSwitch.updateConnexions();
    }
}
