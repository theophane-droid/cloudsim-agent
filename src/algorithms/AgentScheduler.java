package algorithms;

import network.AgentDatacenter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * This class allow user to send the agent every cycleDuration seconds;
 * @author Théophane Dumas
 */
public class AgentScheduler extends SimEntity {
    private AgentDatacenter dc;
    private double cycleDuration;

    public AgentScheduler(String name, AgentDatacenter dc, double cycleDuration) {
        super(name);
        this.dc = dc;
        this.cycleDuration = cycleDuration;
        cycle();
    }

    /**
     * This method allow the class to add an event to CloudSIm, this event will send the Agent when it comes back to the instance
     */
    private void cycle(){
        CloudSim.send(getId(), getId(), cycleDuration, CloudSimTags.NextCycle, null);
    }


    /**
     * When event comes back, we send the Agent
     * @param simEvent
     */
    @Override
    public void processEvent(SimEvent simEvent) {
        if(simEvent.getTag()==CloudSimTags.NextCycle){
            dc.sendAgent();
            cycle();
        }
    }

    @Override
    public void startEntity() {}

    @Override
    public void shutdownEntity() {}
}
