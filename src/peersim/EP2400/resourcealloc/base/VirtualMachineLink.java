package peersim.EP2400.resourcealloc.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peersim.EP2400.resourcealloc.base.types.VirtualMachineLinkType;
import peersim.config.Configuration;

public class VirtualMachineLink {
	private List<VirtualMachine> vmlist;	
	
	private VirtualMachineLinkType lnkType;
	
	
	



	public double getNetworkDemand() {
		return networkDemand;
	}


	public void setNetworkDemand(double networkDemand) {
		this.networkDemand = networkDemand;
	}


	public double getNetworkLimit() {
		return networkLimit;
	}


	public void setBandwidthLimit(double networkLimit) {
		this.networkLimit = networkLimit;
	}


	public double getNetworkAllocation() {
		return networkAllocation;
	}


	public void setNetworkAllocation(double networkAllocation) {
		this.networkAllocation = networkAllocation;
	}

	private double networkDemand;
	private double networkLimit;
	private double networkAllocation;

	
	public VirtualMachineLink(VirtualMachine firstVM, VirtualMachine secondVM, VirtualMachineLinkType lnkType)
	{
		vmlist = new ArrayList<VirtualMachine>();
		
		vmlist.add(firstVM);
		vmlist.add(secondVM);
		
		setParameters(lnkType);
		
		
	}
	
	/**
	 * return a boolean whether this network link will consume network resource
	 * Basically, check whether all VMs provided by this link is on the same physical machine
	 * @return
	 */
	public boolean isConsumeNetworkDemand()
	{
		// check whether a VMLink will consume network demand
		boolean consume = false;		
		PhysicalMachine firstMachine = vmlist.get(0).getPhysicalMachine();
		
		assert(vmlist.size() == 2);
		
		for( VirtualMachine vm : vmlist)
		{
			// if it is not on the same machine, set to false
			
			if (vm.getPhysicalMachine() == null )
				continue;
			
			if (!vm.getPhysicalMachine().equals(firstMachine))
				return true;
			
		}
		
		return consume;
		
	}
	
	
	/**
	 * Get virtual machine on the other side
	 * @return
	 */
	public VirtualMachine getOtherVirtualMachine(VirtualMachine askVm)
	{
		if (askVm.equals(vmlist.get(0)))
			return vmlist.get(1);
		else if (askVm.equals(vmlist.get(1)))
	        return vmlist.get(0);
		else
			assert(false);
	        
		return null;
	}
	
	
	
	private void setParameters(VirtualMachineLinkType lnkType)
	{
		// 1 netRefCapacity is 1 Mb/sec
		double netRefCapacity =  Configuration.getDouble("net_ref_capacity");
		this.lnkType = lnkType;
		
		switch (lnkType)
		{
		case VirtualMachineLinkType_Small:
			this.networkLimit  = 1 * netRefCapacity;	
			
			break;
			
		case VirtualMachineLinkType_Medium:
			this.networkLimit  = 10 * netRefCapacity;	
			
			break;
			
		case VirtualMachineLinkType_Large:
			this.networkLimit  = 100 * netRefCapacity;	
			
			break;
		
		
		}
		
		
		
		
	}
	
	
}
