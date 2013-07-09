package peersim.EP2400.resourcealloc.base;

import peersim.config.Configuration;

public class VirtualSwitchManager {

	
	

	private PhysicalMachine machine;

	public PhysicalMachine getMachine() {
		return machine;
	}

	
	public VirtualSwitchManager(PhysicalMachine machine)
	{
		this.machine = machine;
		
	}
	
	public double getCPUConsumption()
	{
		// 1 core consumption
		double cpuRefCapacity =  Configuration.getDouble("cpu_ref_capacity");
		
		double totalNetworkDemand = 0;
		
		for (VirtualMachine vm : machine.vmsList())
		{
			for (VirtualMachineLink link : vm.getVmLinks())
			{
				totalNetworkDemand += link.getNetworkDemand();
			}
		}
		
		double cpuConsume   = totalNetworkDemand * cpuRefCapacity / machine.getNetworkCapacity();
		
		
		return cpuConsume;
		
		
	}
	
	
}
