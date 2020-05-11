package utils;

import network.AgentDatacenter;
import network.AgentHost;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.List;

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
        for(Host h1: dc.getHostList()){
            AgentHost  h = (AgentHost) h1;
            System.out.println("    host " + h.getId() + " : " + "( is active : " + h.isUp() + " )");
            for(Vm v: h.getVmList()){
                System.out.println("        vm " + v.getId());
            }
        }
    }
}
