package algorithms;

import network.AgentDatacenter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * This class allow user to make an action every cycleDuration seconds;
 * @author Théophane Dumas
 */
public class Scheduler extends SimEntity {
    public static double lastDatacenterEvent=0;
    private double cycleDuration;
    private Action action;

    public Scheduler(String name, double cycleDuration, Action action) {
        super(name);
        this.cycleDuration = cycleDuration;
        this.action = action;
        cycle();
    }

    /**
     * This method allow the class to add an event to CloudSIm, this event will call action.action() when it comes back to the instance
     */
    private void cycle(){
        if(CloudSim.clock()-lastDatacenterEvent<100000)
            CloudSim.send(getId(), getId(), cycleDuration, CloudSimTags.NextCycle, null);
    }


    /**
     * When event comes back, we call action.action()
     * @param simEvent
     */
    @Override
    public void processEvent(SimEvent simEvent) {
        if(simEvent.getTag()==CloudSimTags.NextCycle){
            action.action();
            cycle();
        }
    }

    @Override
    public void startEntity() {}

    @Override
    public void shutdownEntity() {}
}
