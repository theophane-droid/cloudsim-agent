package algorithms;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

/**
 * This class allow user to make an action every cycleDuration seconds;
 * @author Théophane Dumas
 */
public class Scheduler extends SimEntity {
    public static List<Cloudlet> cloudletsList;
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
        if(!isFinished())
            CloudSim.send(getId(), getId(), cycleDuration, CloudSimTags.NextCycle, null);
        else
            System.out.println("stop cycle : "  + getName() + " " + CloudSim.clock() );
    }

    /**
     * @return true if all cloudlet are over
     * @throws RuntimeException if cloudletsList is not set
     */
    private boolean isFinished(){
        if(cloudletsList==null)
            throw new RuntimeException("You have to set cloudlet before use a Scheduler");
        for(Cloudlet c: cloudletsList){
            if(c.getStatus()!=Cloudlet.SUCCESS && c.getStatus()!=Cloudlet.FAILED && c.getStatus()!=Cloudlet.FAILED_RESOURCE_UNAVAILABLE)
                return false;
            //System.out.println(c.getStatus());
        }
        System.out.println(cloudletsList);
        return true;
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
