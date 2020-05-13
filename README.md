# Java-agent simulation in Cloudsim
This java project allow to simulate the usage of an java agent in cloud computing. This agent is send to Hosts and Switchs to make VM migration and Switchs optimization.
## 1 Setup
To run this project you need to add the following libraries :

- cloudsim-3.0.3
- hamc rest-2.2
- Junit-4.12
- mockito-all-1.9.5
- ini4j

Others versions could works but didn't be tested. All the librairies are in the lib folder.

## 2 Organization of the project

1. res : contains resources and config files for the simulation

2. src: contains the simulation sources

   ​	2.1: algorithms: contains the Agent and few useful classes for it

   ​	2.2: network: contains classes to give network capability

   ​	2.3: power: contains classes to simulate power consumption (for now it just contains AgentSwitchPowerModel)

   ​	2.4: simulations: contains classes related to simulation, the Main file dynamically call the proper Simulation (depend on the config.ini file)
        
        2.5: utils: contains a Utils class which contains differents usefull methods. Contains a Vars class with a lot of globals vars defined in it.

3. test: contains classes tests 
4. lib: contains the lib of the project

## 3 Run

To run a first simulation just use the src/Main.java class as entry point.

## 4 Change the parameters

Look at the simulation.ini file to change simulation parameters like detection method (time based or event based), number of host, number of cloudlets, number of vms... 

Note : CloudSim time doesn't match real time.

### 4.1 Simulation parameters [simulation]

- name: the name of the simulation
- print_datacenter: can be true or false; active or not the display of the datacenter state

### 4.2 Vars [vars]

- power_meseare_interval: an integer greater that 0. It represent the number of secs between 2 power measure.  The larger this variable is, the slower the simulation but the more accurate the measurement. 

### 4.3 Datacenter [datacenter]

- nb_hosts: the number of hosts in the datacenter
- nb_vms: the number of vms
- nb_cloudlet: the number of  cloudlets

Note: the switchs are automatically created and connected to hosts.

### 4.4 Agent [agent]

- detection_method: can be TimeBased or DaemonBased. For TimeBased every x secs the agent will be sent to every hosts and switchs. For a DaemonBased method, when the cpu utilization ratio get out bound, it ask to get the agent.
- mips_agent_utilization: that represent the cost of the agent in mips for a cpu. This variable is only took in account for hosts. 
- bw_agent_utilization: that represent the cost of the agent in bandwidth. This variable is only took in account for switchs 

### 4.5 TimeBased [TimeBased]

The following parameters are took in account when the detection method is TimeBased.

- repeating_time: an integer greater than 0. It represent the number of sec between two agent's dispatches

### 4.6 DaemonBased [DaemonBased]

The following parameters are took in account when the detection method is DaemonBased.

- mips_daemon_utilization: an integer greater than 0. That represent the mips cost of a daemon on an host.
-  lower_bound_ratio: a float between 0 and 1. The lower bound of the ratio. If a cpu utilization ratio is lower than this, the host will ask to get the agent.
- upper_bound_ratio: a float between 0 and 1. The upper bound of the ratio. If a cpu utilization ratio is upper than this, the host will ask to get the agent.



## 5 Continuing the development

To continue the development of the simulations, please try to use Agent classes because they are designed to work together.

