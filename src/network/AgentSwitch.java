package network;

import algorithms.Agent;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.power.PowerHost;
import power.AgentSwitchPowerModel;

import java.util.*;

/**
 * A class wich allow to recieved RawPaquet, and to switch off/on ports
 * Warning : don't use hostlist and uplinkswitch
 * @author Théophane Dumas
 */
public class AgentSwitch extends SimEntity {
    private final int nbPorts;
    protected List<RawPacket> packetsToSort;
    protected List<RawPacket> packetsRecieved;
    private List<Port> hostConnexions;
    private List<Port> upSwitchConnexions;
    private boolean isActive = true;
    public Map<Integer, AgentHost> hostlist =  new HashMap<>();
    public List<AgentSwitch> uplinkswitches = new ArrayList<>();
    private Map<Double, Pair<Double, Integer>> usageHistory = new HashMap<>();
    private Datacenter dc;
    private double switching_delay;
    private AgentSwitchPowerModel powerModel;
    private List<Pair<Double, Double>> powerConsumptionHistory; // a pair : <time, power-value>
    private double traffic;
    private double bandwidth;

    /**
     * Constructor for AgentSwitch
     *
     * @see EdgeSwitch#EdgeSwitch(String, int, NetworkDatacenter)
     */
    public AgentSwitch(Datacenter dc, int nbPorts, String name, AgentSwitchPowerModel powerModel, double bandwidth) {
        super(name);
        packetsToSort = new ArrayList<>();
        packetsRecieved = new ArrayList<>();
        hostConnexions = new ArrayList<>();
        upSwitchConnexions = new ArrayList<>();
        powerConsumptionHistory = new ArrayList<>();
        this.nbPorts = nbPorts;
        this.dc=dc;
        this.switching_delay= NetworkConstants.SwitchingDelayEdge;
        this.powerModel = powerModel;
        this.bandwidth=bandwidth;
    }

    @Override
    public void startEntity() {
        Log.printLine(this.getName() + " is starting...");
        this.schedule(this.getId(), 0.0D, 15);
    }

    /**
     * @see EdgeSwitch#processEvent
     * @param ev
     */
    @Override
    public void processEvent(SimEvent ev) {
        if(ev.getData() instanceof RawPacket) {
            packetsToSort.add((RawPacket) ev.getData());
            processPackets(packetsToSort);
            readRecievedPackets();
        }
    }

    @Override
    public void shutdownEntity() {

    }

    /**
     * Transferring or read RawPackets
     * @param packetsToSort
     */
    protected void processPackets(List<RawPacket> packetsToSort) {
        while (packetsToSort.size() > 0) {
            RawPacket rawPacket = packetsToSort.get(0);
            rawPacket.decrementTTL();
            if (rawPacket.getTTL() > 0) {
                if (rawPacket.getClassDest() == getClass() && rawPacket.getIdDest() == getId())
                    packetsRecieved.add(rawPacket);
                else
                    sendRawPaquet(rawPacket);

            }
            packetsToSort.remove(0);
        }
    }
    /**
     * Read packets destined for the switch
     */
    public void readRecievedPackets(){
        while(packetsRecieved.size()>0){
            RawPacket rawPacket = packetsRecieved.get(0);
            rawPacket.setRecievedBy(this);
            if(rawPacket.getContent() instanceof Agent){
                ((Agent)rawPacket.getContent()).action(this);
            }
            packetsRecieved.remove(0);
        }
    }

    /**
     * Send a rawPacket to the next switch or the destinated host
     * @param rawPacket packet to send
     */
    public void sendRawPaquet(RawPacket rawPacket){
        boolean hasBeenSended = false;
        if(rawPacket.getClassDest()!= null && AgentHost.class.isAssignableFrom(rawPacket.getClassDest())) {
            for (int i : hostlist.keySet()) {
                if (rawPacket.getIdDest() == i){
                    if(!(hostlist.get(i) instanceof AgentHost)){
                        throw new RuntimeException("Raw packet can only be adressed to an AgentHost");
                    }
                    hostlist.get(i).getPacketsToSort().add(rawPacket);
                    hasBeenSended=true;
                }
            }
        }
        if(!hasBeenSended)
            CloudSim.send(dc.getId(), uplinkswitches.get(0).getId(), switching_delay, CloudSimTags.Network_Event_UP, rawPacket);
    }

    /**
     * method to reset connexions based on what port is open. This method must be called after any changement on hostConnexions or upSwitchConnexion
     */
    public void updateConnexions(){
        uplinkswitches = new ArrayList<>();
        hostlist = new HashMap<>();
        for (Port p: hostConnexions) {
            if (p.isOpen())
                hostlist.put(((Host) p.getReliedObject()).getId(), (AgentHost) p.getReliedObject());
        }
        for(Port p: upSwitchConnexions){
            if(p.isOpen())
                uplinkswitches.add((AgentSwitch) p.getReliedObject());
        }
    }

    /**
     * @return a list which contain : <a true boolean if related host is up; the related port>
     */
    public List<Pair<Boolean, Port>> sortUsedAndUnusedConnexions(){
        PowerHost h;
        List<Pair<Boolean, Port>> list = new ArrayList<>();
        for(Port p: hostConnexions){
            h = (PowerHost)p.getReliedObject();
            boolean isUp = h.getStateHistory().get(h.getStateHistory().size()-1).isActive();
            list.add(new Pair<>(isUp, p));
        }
        return list;
    }

    public List<Port> getHostConnexions() {
        return hostConnexions;
    }

    public List<Port> getUpSwitchConnexions() {
        return upSwitchConnexions;
    }

    public void refreshUpSwitchConnexions(List<RawPacket> packetsToSort) {
        this.upSwitchConnexions = new ArrayList<>();
    }

    public void setIsActive(boolean b) {
        this.isActive = b;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getNbPorts() {
        return nbPorts;
    }

    public double getMbps() {
        return 0;
    }

    public double getTraffic() {
        return traffic;
    }

    public void updateTraffic(){
        traffic = 0;
        int key=0;
        for (Iterator<Integer> i = hostlist.keySet().iterator(); i.hasNext();){
            key = i.next();
            traffic += hostlist.get(key).getMeanTraffic();
        }
    }
    public void updatePowerConsumption() {
        updateConnexions();
        updateTraffic();
        if(isActive)
            powerConsumptionHistory.add(new Pair(CloudSim.clock(), powerModel.getPowerConsumption(this)));
        else
            powerConsumptionHistory.add(new Pair(CloudSim.clock(),0.d));
    }

    /**
     * This method calculate approximately the total power consumption
     * @return total consumption
     */
    public double calcTotalPowerConsuption(){
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

    public List<RawPacket> getPacketsToSort() {
        return packetsToSort;
    }

    public List<Pair<Double, Double>> getPowerConsumptionHistory() {
        return powerConsumptionHistory;
    }
}
