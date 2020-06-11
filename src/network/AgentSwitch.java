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
import utils.Utils;
import utils.Vars;

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
    private List<Port> downSwitchConnexions;
    private boolean isActive = true;
    public Map<Integer, AgentHost> hostlist =  new HashMap<>();
    public List<AgentSwitch> uplinkswitches = new ArrayList<>();
    public List<AgentSwitch> downlinkswitches = new ArrayList<>();
    public boolean constant = false;
    private Map<Double, Pair<Double, Integer>> usageHistory = new HashMap<>();
    private Datacenter dc;
    private double switching_delay;
    private AgentSwitchPowerModel powerModel;
    private List<Pair<Double, Double>> powerConsumptionHistory; // a pair : <time, power-value>
    private double traffic;
    private double bandwidth;
    // * is this var = true, the simulation should be DaemonBased
    private boolean isRunningDaemon = false;
    // * this var rpz a bandwitch consumption increase for a short time

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
        downSwitchConnexions = new ArrayList<>();
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
    public void shutdownEntity() {}

    /**
     * This private method allow to check if the packet contains an Agent. If it does, it will add bandwidth consumption due to the agent.
     * @param packet the concerned packet
     */
    private void checkIfAgent(RawPacket packet){
        if(packet.getContent() instanceof Agent){
            addToTraffic(Vars.BW_AGENT_UTILIZATION);
        }
    }

    /**
     * Transferring or read RawPackets
     * @param packetsToSort packet the concerned list
     */
    protected void processPackets(List<RawPacket> packetsToSort) {
        while (packetsToSort.size() > 0) {
            RawPacket rawPacket = packetsToSort.get(0);
            rawPacket.decrementTTL();
            checkIfAgent(rawPacket);
            if (rawPacket.getTTL() > 0) {
                if (rawPacket.getClassDest() == getClass() && rawPacket.getIdDest() == getId()) {
                    packetsRecieved.add(rawPacket);
                }
                else {
                    sendRawPaquet(rawPacket);
                }
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
                    hostlist.get(i).processPackets();
                    hasBeenSended=true;
                }
            }
        }
        if(!hasBeenSended) {
            // * TODO: finir ça
            if(isInSon(rawPacket.getIdDest(), rawPacket.getClassDest())){
                List<AgentSwitch> sonsWichCanReachDest = getSonsWichContains(rawPacket.getIdDest(), rawPacket.getClassDest());
                if(rawPacket.getContent() instanceof Agent)
                    propagateDownBandwidthConsumtion(Vars.BW_AGENT_UTILIZATION, rawPacket.getIdDest(), rawPacket.getClassDest());
                // * the first one commute the packet
                CloudSim.send(dc.getId(), sonsWichCanReachDest.get(0).getId(), switching_delay, CloudSimTags.Network_Event_UP, rawPacket);
            }
            else{
                // * we send the packet to the upper switch and we increase the traffic of the upper switch (if the content is an agent)
                if(!getName().equals("router")){
                    if(rawPacket.getContent() instanceof Agent)
                        uplinkswitches.get(0).addToTraffic(getTraffic()+Vars.BW_AGENT_UTILIZATION);
                    CloudSim.send(dc.getId(), uplinkswitches.get(0).getId(), switching_delay, CloudSimTags.Network_Event_UP, rawPacket);
                }
            }
        }
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
        for(Port p: downSwitchConnexions){
            if (p.isOpen()){
                downlinkswitches.add((AgentSwitch)p.getReliedObject());
            }
        }
    }

    /**
     * @return a list which contain : <a true boolean if related host is up; the related port>
     */
    public List<Pair<Boolean, Port>> sortUsedAndUnusedConnexions(){
        AgentHost h;
        List<Pair<Boolean, Port>> list = new ArrayList<>();
        for(Port p: hostConnexions){
            h = (AgentHost)p.getReliedObject();
            boolean isUp = h.isUp();
            list.add(new Pair<>(isUp, p));
        }
        return list;
    }

    public boolean isActive(PowerHost h) {
        return h.getStateHistory().get(h.getStateHistory().size()-1).isActive();
    }

    public List<Port> getHostConnexions() {
        return hostConnexions;
    }

    public List<Port> getUpSwitchConnexions() {
        return upSwitchConnexions;
    }

    public List<Port> getAllSwitchConnexions(){
        List<Port> l = new ArrayList<>();
        l.addAll(downSwitchConnexions);
        l.addAll(upSwitchConnexions);
        return l;
    }

    public List<Port> getDownSwitchConnexions() {
        return downSwitchConnexions;
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
        for (Iterator<Integer> i = hostlist.keySet().iterator(); i.hasNext();) {
            key = i.next();
            traffic += hostlist.get(key).getTraffic();
        }
    }
    public void updatePowerConsumption() {
        updateConnexions();
        if(isActive)
            powerConsumptionHistory.add(new Pair<>(CloudSim.clock(), powerModel.getPowerConsumption(this)));
        else
            powerConsumptionHistory.add(new Pair<>(CloudSim.clock(),0.d));
        optimizeConsumption();
    }

    private boolean checkDaemon() {
        boolean modification = false;
        List<Pair<Boolean, Port>> list = sortUsedAndUnusedConnexions();
        int nbHostUp = 0;
        for(Pair<Boolean, Port> p: list){
            modification = p.getFirst() != p.getSecond().isOpen() || modification;
            if(p.getFirst())
                nbHostUp+=1;
        }
        modification = nbHostUp!=0 != isActive() || modification;
        if(modification)
            ((AgentDatacenter)dc).sendAgentTo((AgentHost) dc.getHostList().get(dc.getHostList().size()-1), this);
        return modification;
    }

    /**
     * This method allow agent switch to optimize his own consumption by himself
     */
    private void optimizeConsumption(){
        boolean shouldBeShutDown = true;
        if(isAccess()) {
            List<Pair<Boolean, Port>> l = sortUsedAndUnusedConnexions();
            for (Pair<Boolean, Port> p : l) {
                shouldBeShutDown = p.getFirst() || !shouldBeShutDown;
                p.getSecond().setOpen(p.getFirst());
            }
        }
        if(isAggregation() && isRunningDaemon){
            if(getUtilization()<Vars.DAEMON_SWITCH_LOWER_BOUND || getUtilization()>Vars.DAEMON_SWITCH_UPPER_BOUND){
                ((AgentDatacenter)dc).sendAgentTo((AgentHost) dc.getHostList().get(0), this);
            }
        }
    }

    /**
     * This method calculate approximately the total power consumption
     * @return total consumption
     */
    public double calcTotalPowerConsuption(){
        return Utils.calcPowerConsumtion(powerConsumptionHistory);
    }

    public List<RawPacket> getPacketsToSort() {
        return packetsToSort;
    }

    public List<Pair<Double, Double>> getPowerConsumptionHistory() {
        return powerConsumptionHistory;
    }

    /**
     * If you call this method, that activates the daemon wich will call the Agent under lower bound and overhead upper bound of cpu utilization
     */
    public void startDaemon(){
        if(Vars.DAEMON_HOST_LOWER_BOUND <0 || Vars.DAEMON_HOST_LOWER_BOUND >1){
            throw new RuntimeException("Vars.DAEMON_LOWER_SHOULD be between 0 and 1, check the simulation.ini file");
        }
        if(Vars.DAEMON_HOST_UPPER_BOUND <0 || Vars.DAEMON_HOST_UPPER_BOUND >1){
            throw new RuntimeException("Vars.DAEMON_UPPER_BOUND be between 0 and 1, check the simulation.ini file");
        }
        if(Vars.DAEMON_HOST_UPPER_BOUND <=Vars.DAEMON_HOST_LOWER_BOUND){
            throw new RuntimeException("Vars.DAEMON_UPPER_BOUND should be greater that Vars.DAEMON_LOWER_BOUND, check the simulation.ini file");
        }
        isRunningDaemon = true;
    }

    /**
     * This method allow user to increase momentarily the Bw consumption
     * @param d
     */
    public void addToTraffic(double d){
        traffic+=d;
    }

    /**
     * This method is used to propagate up the traffic in the network, from a cloudlet on an host to others switchs
     * @param consumption consumption
     */
    public void propagateUpBandwidthConsumtion(double consumption){
        List<AgentSwitch> reachablesSwitchs = new ArrayList<>();
        for(Port p: getUpSwitchConnexions()){
            if(p.isOpen() && ((AgentSwitch)p.getReliedObject()).isActive()){
                reachablesSwitchs.add((AgentSwitch) p.getReliedObject());
            }
        }
        for(AgentSwitch sw: reachablesSwitchs){
            sw.propagateUpBandwidthConsumtion(consumption/reachablesSwitchs.size());
        }
        traffic+=consumption;
    }

    /**
     * This method is used to propagate down the traffic in the network, for agentswitch wich can reach an host
     * @param consumption consumption
     * @param idDest the id of the host
     * @param classDest the class of the host
     */
    public void propagateDownBandwidthConsumtion(double consumption, int idDest, Class classDest){
        List<AgentSwitch> reachablesSwitchs = getSonsWichContains(idDest, classDest);
        for(AgentSwitch sw: reachablesSwitchs)
            sw.addToTraffic(consumption/reachablesSwitchs.size());
        traffic+=consumption;
    }

    /**
     * This method return true if a switch or an host is present in sons
     * @param id the id of the entity
     * @param classe the class of the entity
     * @return true if h is present
     */
    protected boolean isInSon(int id, Class classe) {
        if(id==getId() && classe==getClass())
            return true;
        if(classe==AgentHost.class) {
            for (int i : hostlist.keySet()) {
                if(i == id)
                    return true;
            }
        }
        for (AgentSwitch son : downlinkswitches){
            if (son.isActive() && son.isInSon(id, classe))
                return true;
        }
        return false;
    }

    /**
     * This method return the list of the downer AgentSwitch wich contains an AgentHost (for which the Port is open and the switch is up)
     * @param id the id of the AgentHost
     * @param classe the classe of the AgentHost
     * @return the list
     */
    protected List<AgentSwitch> getSonsWichContains(int id, Class classe){
        List<AgentSwitch> r = new ArrayList<>();
        for(AgentSwitch a: downlinkswitches){
            if(a.isInSon(id, classe) && a.isActive())
                r.add(a);
        }
        return r;
    }

    /**
     * @return true if it's an access switch
     */
    public boolean isAccess(){
        return getName().contains("access");
    }

    /**
     * @return true if i's a core switch
     */
    public boolean isCore(){
        return getName().contains("core");
    }

    /**
     * @return true if i's an aggregation switch
     */
    public boolean isAggregation(){return getName().contains("aggregation");}

    public double getUtilization(){
        return traffic/bandwidth;
    }

    public void setTraffic(double traffic) {
        this.traffic = traffic;
    }
    public boolean isUnderUtilized(){
        return getUtilization()<Vars.DAEMON_SWITCH_LOWER_BOUND;
    }
    public boolean isOverUtilized(){
        return getUtilization()>Vars.DAEMON_SWITCH_UPPER_BOUND;
    }
}
