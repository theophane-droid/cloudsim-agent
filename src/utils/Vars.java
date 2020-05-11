package utils;

import org.ini4j.Wini;

public class Vars {
    // * followings vars can be changed from the ini file in the vars section
    public static double SAFETY_PARAMETER = 0.7;
    public static double POWER_MEASURE_INTERVAL = 100;
    public static double MEAN_CLOUDLET_BW_CONSUMPTION = 200;
    // * followings can be changed from the DaemonBased section
    public static float DAEMON_UPPER_BOUND=-1;
    public static float DAEMON_LOWER_BOUND=-1;
    public static void loadFromIniFile(Wini ini){
        SAFETY_PARAMETER = setDouble(ini,"SAFETY_PARAMETER", SAFETY_PARAMETER);
        POWER_MEASURE_INTERVAL = setDouble(ini,"POWER_MEASURE_INTERVAL",POWER_MEASURE_INTERVAL);
        MEAN_CLOUDLET_BW_CONSUMPTION = setDouble(ini,"MEAN_CLOUDLET_BW_CONSUMPTION",MEAN_CLOUDLET_BW_CONSUMPTION);
        System.out.println(SAFETY_PARAMETER);
        System.out.println(POWER_MEASURE_INTERVAL);
        System.out.println(MEAN_CLOUDLET_BW_CONSUMPTION);
    }
    private static double setDouble(Wini ini, String k2, double val){
        double d = ini.get("vars", k2, Double.TYPE);
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