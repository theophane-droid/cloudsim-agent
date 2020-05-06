package network;

import algorithms.Agent;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.List;

/**
 * A class wich allows RawPackets to be transferred to AgentSwitch.
 * @author Théophane DUmas
 */
public class AgentHost extends PowerHostUtilizationHistory{
    private List<RawPacket> packetsToSort;
    private List<RawPacket> packetsRecieved;
    private AgentSwitch sw;
    public double bandwidth;

    public AgentHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        packetsToSort = new ArrayList<>();
        packetsRecieved = new ArrayList<>();
    }



    /**
     * @see Host#updateVmsProcessing(double)
     */
    @Override
    public double updateVmsProcessing(double currentTime) {
        processPackets();
        readRecievedPackets();
        return super.updateVmsProcessing(currentTime);
    }

    /**
     * Transferring or read RawPackets
     */
    public void processPackets(){
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
     * Send a rawPaquet to the next switch
     * @param rawPacket
     */
    public void sendRawPaquet(RawPacket rawPacket){
        CloudSim.send(getDatacenter().getId(), sw.getId(), 100, CloudSimTags.Network_Event_UP, rawPacket);
    }

    /**
     * Read packets destined for the host
     */
    public void readRecievedPackets(){
        while(packetsRecieved.size()>0){
            RawPacket rawPacket =packetsRecieved.remove(0);
            rawPacket.setRecievedBy(this);
            if(rawPacket.getContent() instanceof Agent) {
                ((Agent) rawPacket.getContent()).action(this);
            }
        }
    }
    /**
     * @return packetsToSort list
     */
    public List<RawPacket> getPacketsToSort() {
        return packetsToSort;
    }

    public AgentSwitch getSw() {
        return sw;
    }

    public void setSw(AgentSwitch sw) {
        this.sw = sw;
    }
}
