package network;

import algorithms.Agent;
import org.cloudbus.cloudsim.*;
import utils.Utils;
import utils.Vars;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
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
    // * both following variables are usefull for modeling switch power consumption
    private double bwConsumption=-1;
    public double meanTraffic;
    private List<Pair<Double, Double>> powerConsumptionHistory; // a pair : <time, power-value>
    // * is this var = true, the simulation should be DaemonBased
    private boolean isRunningDaemon = false;

    public boolean agentAsBeenRunning = false;

    public AgentHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        packetsToSort = new ArrayList<>();
        packetsRecieved = new ArrayList<>();
        powerConsumptionHistory = new ArrayList<>();
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
            RawPacket rawPacket = packetsToSort.remove(0);
            if(rawPacket.getClassDest()==getClass() && rawPacket.getIdDest()==getId()){
                packetsRecieved.add(rawPacket);
                readRecievedPackets();
            }
            else{
                sendRawPaquet(rawPacket);
            }
        }
    }

    /**
     * Send a rawPaquet to the next switch
     * @param rawPacket
     */
    public void sendRawPaquet(RawPacket rawPacket){
        if(rawPacket.getContent() instanceof Agent)
            sw.addToTraffic(Vars.BW_AGENT_UTILIZATION);
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


    public double getTraffic() {
        updateBwConsumption();
        return bwConsumption;
    }

    private void updateBwConsumption() {
        bwConsumption=0;
        for(Vm ignored : getVmList())
            bwConsumption+= Vars.MEAN_CLOUDLET_BW_CONSUMPTION*ignored.getCloudletScheduler().runningCloudlets();
    }

    public void setSw(AgentSwitch sw) {
        this.sw = sw;
    }

    public void updatePowerConsumption() {
        if(getStateHistory().size()==0 || getStateHistory().get(getStateHistory().size()-1).isActive()) {
            powerConsumptionHistory.add(new Pair(CloudSim.clock(), getPower()));
            if(isRunningDaemon){
                checkDaemon();
            }
            agentAsBeenRunning=false;
        }
        else
            powerConsumptionHistory.add(new Pair(CloudSim.clock(),0.d));
    }

    /**
     * This method will ask to send the agent to this host if the utilization of cpu if under of upper the bounds
     * @return true if the utilization is out the bounds
     **/
    private boolean checkDaemon() {
        if(getUtilizationOfCpu()<Vars.DAEMON_HOST_LOWER_BOUND || getUtilizationOfCpu()>Vars.DAEMON_HOST_UPPER_BOUND){
            ((AgentDatacenter)getDatacenter()).sendAgentTo((AgentHost) getDatacenter().getHostList().get(0), this);
            return true;
        }
        return false;
    }

    /**
     * This method calculate approximately the total power consumption
     * @return total consumption
     */
    public double calcTotalPowerConsuption(){
        return Utils.calcPowerConsumtion(powerConsumptionHistory);
    }

    public boolean isUp(){
        if(getStateHistory().size()==0)
            return false;
        return getStateHistory().get(getStateHistory().size()-1).isActive();
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

    @Override
    public double getUtilizationOfCpu() {
        double d=super.getUtilizationOfCpu();
        if(isRunningDaemon && isUp()) {
            d += Vars.MIPS_DAEMON_UTILIZATION / getTotalMips();
        }
        if(agentAsBeenRunning) {
            d += Vars.MIPS_AGENT_UTILIZATION / getTotalMips();
        }
        if(d>1)
            return 1;
        return d;
    }

    public void updateTrafficPropagation() {
        int nbCloudlets = 0;
        for(Vm v : getVmList()){
            nbCloudlets+= v.getCloudletScheduler().runningCloudlets();
        }
        sw.propagateUpBandwidthConsumtion(Vars.MEAN_CLOUDLET_BW_CONSUMPTION*nbCloudlets);
    }
    protected double getPower(double utilization) {
        double power = 0.0D;

        try {
            double temp = utilization>1 ? 1: utilization;
            power = this.getPowerModel().getPower(temp);
        } catch (Exception var6) {
            var6.printStackTrace();
            System.exit(0);
        }

        return power;
    }


}
