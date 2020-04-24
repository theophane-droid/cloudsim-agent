package network;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

import java.util.ArrayList;
import java.util.List;

/**
 * A class wich allow to recieved RawPaquet
 * @author Théophane Dumas
 */
public class AgentSwitch extends EdgeSwitch implements AgentActionner {
    private List<RawPacket> packetsToSort;
    private List<RawPacket> packetsRecieved;
    /**
     * Constructor for AgentSwitch
     * @see EdgeSwitch#EdgeSwitch(String, int, NetworkDatacenter)
     */
    public AgentSwitch(String name, int level, NetworkDatacenter dc) {
        super(name, level, dc);
        packetsToSort = new ArrayList<>();
        packetsRecieved = new ArrayList<>();
    }

    /**
     * @see EdgeSwitch#processEvent
     * @param ev
     */
    @Override
    public void processEvent(SimEvent ev) {
        if(ev.getData() instanceof RawPacket) {
            packetsToSort.add((RawPacket) ev.getData());
            processPackets();
            readRecievedPackets();
        }
        else
            super.processEvent(ev);
    }

    /**
     * Transferring or read RawPackets
     */
    private void processPackets(){
        while(packetsToSort.size()>0){
            RawPacket rawPacket = packetsToSort.get(0);
            if(rawPacket.getClassDest()==getClass() && rawPacket.getIdDest()==getId()){
                packetsRecieved.add(rawPacket);
            }
            else{
                sendRawPaquet(rawPacket);
            }
            packetsToSort.remove(0);
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
            CloudSim.send(dc.getId(), uplinkswitches.get(0).getId(), 100, CloudSimTags.Network_Event_UP, rawPacket);
    }
}
