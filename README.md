# Java-agent simulation in Cloudsim
This java project allow to simulate the usage of an java agent in cloud computing. This agent is send to Hosts and Switchs to make VM migration and Switchs optimization.
## 1 Setup
To run this project you need to add the following libraries :

- cloudsim-3.0.3
- hamc rest-2.2
- Junit-4.12
- mockito-all-1.9.5
- ini4j

Others versions could works but didn't be tested.

## 2 Organization of the project

1. res : contains resources and config files for the simulation

2. src: contains the simulation sources

   ​	2.1: algorithms: contains the Agent and few useful classes for it

   ​	2.2: network: contains classes to give network capability

   ​	2.3: power: contains classes to simulate power consumption (for now it just contain AgentSwitchPowerModel)

   ​	2.4: simulations: contains classes related to simulation, the Main file dynamically call the proper Simulation (depend on the config.ini file)

3. test: contains classes tests 
4. lib: contains the lib of the project



## 3 Run

To run a first simulation just use the src/Main.java class as entry point.

## 4 Change the parameters

Look at the simulation.ini file to change simulation parameters like detection method (time based or event based), number of host, number of cloudlets, number of vms... 