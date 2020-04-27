package network;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.network.datacenter.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class wich allow to recieved RawPaquet, and to switch off/on ports
 * Warning : don't use hostlist and uplinkswitch
 * @author Théophane Dumas
 */
public class AgentSwitch extends EdgeSwitch implements AgentActionner {
    protected List<RawPacket> packetsToSort;
    protected List<RawPacket> packetsRecieved;
    private List<Port> hostConnexions;
    private List<Port> upSwitchConnexions;
    private boolean isActive = true;
    /**
     * Constructor for AgentSwitch
     * @see EdgeSwitch#EdgeSwitch(String, int, NetworkDatacenter)
     */
    public AgentSwitch(String name, int level, NetworkDatacenter dc) {
        super(name, level, dc);
        packetsToSort = new ArrayList<>();
        packetsRecieved = new ArrayList<>();
        hostConnexions = new ArrayList<>();
        upSwitchConnexions = new ArrayList<>();
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
        else
            super.processEvent(ev);
    }

    /**
     * Transferring or read RawPackets
     * @param packetsToSort
     */
    protected void processPackets(List<RawPacket> packetsToSort) {
        if (isActive) {
            while (packetsToSort.size() > 0) {
                RawPacket rawPacket = packetsToSort.get(0);
                rawPacket.ttl -= 1;
                if (rawPacket.ttl > 0) {
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
        if(rawPacket.getClassDest()== AgentHost.class) {
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
    }

    /**
     * method to reset connexions based on what port is open. This method must be called after any changement on hostConnexions or upSwitchConnexion
     */
    public void updateConnexions(){
        uplinkswitches = new ArrayList<>();
        hostlist = new HashMap<>();
        for (Port p: hostConnexions) {
            if (p.isOpen())
                hostlist.put(((Host) p.getReliedObject()).getId(), (NetworkHost) p.getReliedObject());
        }
        for(Port p: upSwitchConnexions){
            if(p.isOpen())
                uplinkswitches.add((Switch) p.getReliedObject());
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
}
