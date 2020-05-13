package utils;

import org.ini4j.Wini;

/**
 * This class define different globals variables, everything can be changed from the ini file. If a variable is not defined in the ini file, it will takes the value bellow.
 * @author Théophane Dumas
 */
public class Vars {
    // * followings vars can be changed from the ini file in the agent section
    public static double BW_AGENT_UTILIZATION = 20;
    public static double MIPS_AGENT_UTILIZATION = 200;
    // * followings vars can be changed from the ini file in the vars section
    public static double SAFETY_PARAMETER = 0.7;
    public static double POWER_MEASURE_INTERVAL = 100;
    public static double MEAN_CLOUDLET_BW_CONSUMPTION = 200;
    // * followings can be changed from the DaemonBased section
    public static float DAEMON_UPPER_BOUND=-1;
    public static float DAEMON_LOWER_BOUND=-1;
    public static double MIPS_DAEMON_UTILIZATION = 100;
    // * cloudlets section
    public static double MEAN_CLOUDLET_LENGTH = 1000;
    public static double STANDARD_CLOUDLET_DEVIATION = 100;
    public static void loadFromIniFile(Wini ini){
        SAFETY_PARAMETER = setDouble(ini,"SAFETY_PARAMETER".toLowerCase(), SAFETY_PARAMETER);
        POWER_MEASURE_INTERVAL = setDouble(ini,"POWER_MEASURE_INTERVAL".toLowerCase(),POWER_MEASURE_INTERVAL);
        MEAN_CLOUDLET_BW_CONSUMPTION = setDouble(ini,"MEAN_CLOUDLET_BW_CONSUMPTION".toLowerCase(),MEAN_CLOUDLET_BW_CONSUMPTION);
        MIPS_DAEMON_UTILIZATION = setDouble(ini, "DaemonBased","MIPS_DAEMON_UTILIZATION".toLowerCase(), MIPS_DAEMON_UTILIZATION);
        MIPS_AGENT_UTILIZATION = setDouble(ini, "agent","MIPS_AGENT_UTILIZATION".toLowerCase(), MIPS_AGENT_UTILIZATION);
        BW_AGENT_UTILIZATION = setDouble(ini, "agent","BW_AGENT_UTILIZATION".toLowerCase(), BW_AGENT_UTILIZATION);
        MEAN_CLOUDLET_LENGTH = setDouble(ini, "cloudlets", "MEAN_CLOUDLET_LENGTH".toLowerCase(), MEAN_CLOUDLET_LENGTH);
        STANDARD_CLOUDLET_DEVIATION = setDouble(ini, "cloudlets", "STANDARD_CLOUDLET_DEVIATION".toLowerCase(), STANDARD_CLOUDLET_DEVIATION);
    }
    private static double setDouble(Wini ini, String k2, double val){
        double d = ini.get("vars", k2, Double.TYPE);
        if(Math.abs(d)>0.0001)
            return d;
        else
            return val;
    }
    private static double setDouble(Wini ini, String k1, String k2, double val){
        double d = ini.get(k1, k2, Double.TYPE);
        if(Math.abs(d)>0.0001)
            return d;
        else
            return val;
    }
    private static float setFloat(Wini ini, String k1, String k2){
        float f = ini.get("vars", k2, Float.TYPE);
        if(Math.abs(f)>0.0001)
            return f;
        else
            return 0;
    }
}