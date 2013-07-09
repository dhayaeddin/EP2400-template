/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.base;

import java.util.ArrayList;

/**
 * A class for representing placement set for each server. For simplicity, this is represented by ArrayList
 * @author Rerngvit Yanggratoke
 */
public class VirtualMachinesList extends ArrayList<VirtualMachine> {

	private static final long serialVersionUID = -2896826171558618679L;

	/**
	 * Constructor which initialize the list and total demand.
	 */
	public VirtualMachinesList()
	{
		
	}
	
	/**
	 * add application to this placement set.
	 */
	public boolean add(VirtualMachine a)
	{
		
		boolean result = super.add(a);
		return result;
		
	}
	
	/**
	 * remove application from this placement set.
	 * @param a
	 */
	public boolean remove(VirtualMachine a)
	{
		boolean result = super.remove(a);
		return result;
	}
	
	
	/**
	 * Retrieve total CPU demand from this placement. 
	 * @return
	 */
	public double totalCPUDemand()
	{
		
		double totalDemand = 0;
		for( VirtualMachine a: this)
		{
			totalDemand += a.getCPUDemand();
		}
		
		
		
		return totalDemand;
	}
	
	public double totalCPUAllocation()
	{
		double totalCPUAllocation = 0;
		for( VirtualMachine a: this)
		{
			totalCPUAllocation += a.getCPUAllocation();
		}
				
		return totalCPUAllocation;
		
		
	}
	
	public double totalMemoryAllocation()
	{
		double totalMemoryAllocation = 0;
		for( VirtualMachine a: this)
		{
			totalMemoryAllocation += a.getMemoryAllocation();
		}
		
		return totalMemoryAllocation;
		
	}
	
	public double totalNetworkAllocation()
	{
		double totalNetworkAllocation = 0;
		for( VirtualMachine vm: this)
		{
			
			for (VirtualMachineLink vmLink : vm.getVmLinks())
				totalNetworkAllocation += vmLink.getNetworkAllocation();
		}
		
		return totalNetworkAllocation;
		
	}
	
	

	/**
	 * Retrieve total memory demand from this placement. 
	 * @return
	 */
	public double totalMemoryDemand()
	{
		
		double totalDemand = 0;
		for( VirtualMachine a: this)
		{
			totalDemand += a.getMemoryDemand();
		}
		
		
		
		return totalDemand;
	}


	
}
