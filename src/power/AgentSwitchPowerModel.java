package power;

import network.AgentSwitch;

/**
 * This power model is based on : "Md Mohaimenul Hossain, Eric Rondeau, Jean-Philippe Georges, Thierry Bastogne.
 * Modeling thepower consumption of Ethernet switch. International SEEDS Conference 2015: Sustainable EcologicalEngineering
 * Design for Society, Sep 2015, Leeds, United Kingdom. ￿hal-01205751"
 * @author Théophane Dumas
 */
public enum AgentSwitchPowerModel {
    CISCO_2960X(35.3642, 0.000389, -0.001006, 0.06646, 0.000213);
    double c;
    double factor1;
    double factor2;
    double factor3;
    double factor4;

    AgentSwitchPowerModel(double c, double factor1, double factor2, double factor3, double factor4) {
        this.c = c;
        this.factor1 = factor1;
        this.factor2 = factor2;
        this.factor3 = factor3;
        this.factor4 = factor4;
    }
    public double getPowerConsumption(AgentSwitch agentSwitch){
        if(agentSwitch.isActive())
            return c + factor1 * agentSwitch.getTraffic() + factor2 * agentSwitch.getMbps() + factor3 * agentSwitch.hostlist.size() +
                    factor4 * (agentSwitch.getMbps()*agentSwitch.hostlist.size());
        return 0;
    }
}
