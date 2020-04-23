import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.RunnerAbstract;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabConstants;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabRunner;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenter;

import java.util.Calendar;
import java.util.List;

/**
 * Class wich run the first simulation process based on planetlab cloudlets
 * @author Théophane Dumas
 */
public class Simulation1Runner extends RunnerAbstract {

    /**
     * Run
     *
     * @param enableOutput       the enable output
     * @param outputToFile       the output to file
     * @param outputFolder       the output folder
     * @param workload           the workload
     */
    public Simulation1Runner(boolean enableOutput, boolean outputToFile, String outputFolder, String workload) {
        super(false, outputToFile, "res/planetlab/", outputFolder, workload, "lr", "mmt", "");
    }

    /**
     * methods wich create switchs and
     */
    private void createNetwork(){

    }

   /**
     * @param inputFolder
     * @see RunnerAbstract#init
    */
    @Override
    protected void init(String inputFolder) {
            try {
                CloudSim.init(1, Calendar.getInstance(), false);
// * my own
                broker = NetworkHelper.createBroker();
                int brokerId = broker.getId();

                cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
                vmList = Helper.createVmList(brokerId, cloudletList.size());
              // * my own
                hostList = NetworkHelper.createHostList(PlanetLabConstants.NUMBER_OF_HOSTS);
            } catch (Exception e) {
                e.printStackTrace();
                Log.printLine("The simulation has been terminated due to an unexpected error");
                System.exit(0);
            }
    }
    /**
     * Starts the simulation.
     *
     * @param experimentName the experiment name
     * @param outputFolder the output folder
     * @param vmAllocationPolicy the vm allocation policy
     */
    protected void start(String experimentName, String outputFolder, VmAllocationPolicy vmAllocationPolicy) {
        System.out.println("Starting " + experimentName);

        try {
            // * my own
            NetworkDatacenter datacenter = (NetworkDatacenter) NetworkHelper.createDatacenter(
                    hostList,
                    vmAllocationPolicy);
            NetworkHelper.createNetwork(10, datacenter);

            //datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
            double lastClock = CloudSim.startSimulation();
            System.out.println("last clock : " + lastClock);

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");

            CloudSim.stopSimulation();

            NetworkHelper.printResults(
                    datacenter,
                    vmList,
                    lastClock,
                    experimentName,
                    Constants.OUTPUT_CSV,
                    outputFolder);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + experimentName);
    }
}
