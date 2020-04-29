package network;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.network.datacenter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class wich allow to recieved RawPaquet, and to switch off/on ports
 * Warning : don't use hostlist and uplinkswitch
 * @author Théophane Dumas
 */
public class AgentSwitch extends SimEntity implements AgentActionner {
    private final int nbPorts;
    protected List<RawPacket> packetsToSort;
    protected List<RawPacket> packetsRecieved;
    private List<Port> hostConnexions;
    private List<Port> upSwitchConnexions;
    private boolean isActive = true;
    public Map<Integer, AgentHost> hostlist =  new HashMap<>();
    public List<AgentSwitch> uplinkswitches = new ArrayList<>();
    // this
    private Map<Double, Pair<Double, Integer>> usageHistory = new HashMap<>();
    private Datacenter dc;
    private double switching_delay;

    /**
     * Constructor for AgentSwitch
     *
     * @see EdgeSwitch#EdgeSwitch(String, int, NetworkDatacenter)
     */
    public AgentSwitch(Datacenter dc, int nbPorts, String name) {
        super(name);
        packetsToSort = new ArrayList<>();
        packetsRecieved = new ArrayList<>();
        hostConnexions = new ArrayList<>();
        upSwitchConnexions = new ArrayList<>();
        this.nbPorts = nbPorts;
        this.dc=dc;
        this.switching_delay= NetworkConstants.SwitchingDelayEdge;
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
        if (isActive) {
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
    }
    /**
     * Read packets destined for the switch
     */
    private void readRecievedPackets(){
        while(packetsRecieved.size()>0){
            RawPacket rawPacket = packetsRecieved.get(0);
            rawPacket.setRecievedBy(this);
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
                    ((AgentHost)hostlist.get(i)).getPacketsToSort().add(rawPacket);
                    hasBeenSended=true;
                }
            }
        }
        if(!hasBeenSended)
            CloudSim.send(dc.getId(), uplinkswitches.get(0).getId(), switching_delay, CloudSimTags.Network_Event_UP, rawPacket);
        System.out.println("uplinkswitch : " + this.getId() + " => " + uplinkswitches);
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

    public List<Port> getHostConnexions() {
        return hostConnexions;
    }

    public List<Port> getUpSwitchConnexions() {
        return upSwitchConnexions;
    }

    public void refreshHostConnexions(List<Port> hostConnexions) {
        this.hostConnexions = new ArrayList<>();
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

    public double getBandwidth() {
        return 0;
    }
}
