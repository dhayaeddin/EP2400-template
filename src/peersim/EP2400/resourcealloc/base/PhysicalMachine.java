/*
 * Copyright (c) 2010 LCN, EE school, KTH
 * 
 */

package peersim.EP2400.resourcealloc.base;

import java.util.HashSet;
import java.util.Set;

import peersim.EP2400.resourcealloc.base.types.PhysicalMachineType;
import peersim.EP2400.resourcealloc.base.types.VirtualDataCenterType;
import peersim.EP2400.resourcealloc.tasks.ResourceAllocationLogic;
import peersim.config.Configuration;
import peersim.core.*;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;

/**
 * <p>
 * This class implements a base for the gossip-based application placement
 * protocol.
 * </p>
 * 
 * @author Rerngvit Yanggratoke
 */
public class PhysicalMachine implements CDProtocol, EDProtocol {

	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	protected String prefix;

	/**
	 * List of running of applications
	 */
	protected VirtualMachinesList vmList;
	
	protected static final String PAR_CPU_CAPACITY_SCALE     = "cpu_capacity_scale";
	protected static final String PAR_MEMORY_CAPACITY_SCALE  = "mem_capacity_scale";
	protected static final String PAR_NETWORK_CAPACITY_SCALE = "net_capacity_scale";
	
	
	
	
	
	
	private VirtualSwitchManager vsm;
	
	private PhysicalMachineType machineType;
	
	public VirtualSwitchManager getVsm() {
		return vsm;
	}


	/**
	 * CPU capacity value set from PlacementInitializer
	 */
	protected double cpuCapacity;
	
	public double getMemoryCapacity() {
		return memoryCapacity;
	}

	public void setMemoryCapacity(double memoryCapacity) {
		this.memoryCapacity = memoryCapacity;
	}

	public double getNetworkCapacity() {
		return networkCapacity;
	}

	public void setNetworkCapacity(double networkCapacity) {
		this.networkCapacity = networkCapacity;
	}

	public double getPowerRate() {
		return powerRate;
	}

	public void setPowerRate(double powerRate) {
		this.powerRate = powerRate;
	}
	
	/**
	 * Compute local resource allocation
	 */
	public void localResourcesAllocation()
	{
		ResourceAllocationLogic.localResourceAllocation(this);
		
	}
	

	protected double memoryCapacity;

	protected double networkCapacity;

	protected double powerRate;
	
	/**
	 * Accessor for the CPU capacity. This is the the same for all servers.
	 * 
	 * @return
	 */
	public double getCpuCapacity() {
		return cpuCapacity;
	}
	
	public double getTotalCPUAllocation()
	{
		double vms_cpuAllocation = vmList.totalCPUAllocation();
		double vsm_cpuAlloc      = vsm.getCPUConsumption();
		
		return vms_cpuAllocation + vsm_cpuAlloc;
	}
	

	public double getTotalMemoryAllocation()
	{
		return vmList.totalMemoryAllocation();
	}
	
	

	public double getTotalNetworkAllocation()
	{
		return vmList.totalNetworkAllocation();
	}
	
	
	
	/**
	 * Get CPU utilization of this machine by dividing cpuCapacity by the total demand of all VMs
	 * @return
	 */
	public double getCPUUtilization()
	{
	
		
		double totalAllocatedCPU  = vmList.totalCPUAllocation();
		return totalAllocatedCPU / cpuCapacity;
		
	}
	
	public void updateLocalDemand()
	{
		
		for (VirtualMachine vm : vmList)
		{
			if (vm.getVdc().getType() == VirtualDataCenterType.VirtualDataCenterType_Interactive)
			{
				double maxCPUDemand = vm.getExpectedCPUMaxDemand();
				
				double u = CommonState.r.nextDouble();
				
				double t = CommonState.getTime();
				double sinInput = (2 * Math.PI * t  / 86400) - (2 * Math.PI * u); 
				double nextCPUDemand = maxCPUDemand * (1.0 + u * Math.sin(sinInput)) / 2.0;
				
				vm.setCPUDemand(nextCPUDemand);
			}
			
			
			
		}
		
		
	}
	
	
	public double getMemoryUtilization()
	{
		double totalAllocatedMemory  = vmList.totalMemoryAllocation();
		return totalAllocatedMemory / memoryCapacity;
		
	}
	
	public double getNetworkUtilization()
	{
		double totalAllocatedNetwork = vmList.totalNetworkAllocation();
		return totalAllocatedNetwork / networkCapacity;
		
	}

	// ------------------------------------------------------------------------
	// Initialization
	// ------------------------------------------------------------------------
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine.
	 * 
	 * @param prefix
	 *            the configuration prefix for this class.
	 */
	public PhysicalMachine(String prefix) {
		this.prefix = prefix;
		vmList = new VirtualMachinesList();		
		vsm = new VirtualSwitchManager(this);
		
		
		
		initCapacity();
	}
	
	
	private void initCapacity()
	{
		double p_type_1 = 0.31;
		double p_type_2 = 0.55;
		double p_type_3 = 0.08;
		
		double r =  CommonState.r.nextDouble();
		
		if (r < p_type_1)
			setCapacity(PhysicalMachineType.PhysicalMachineType_One);
		else
		{
		
			r -= p_type_1;
			
			if (r < p_type_2)
				setCapacity(PhysicalMachineType.PhysicalMachineType_Two);			
			else
			{
				r-= p_type_2;
						
				if (r < p_type_3)
					setCapacity(PhysicalMachineType.PhysicalMachineType_Three);
				else
					setCapacity(PhysicalMachineType.PhysicalMachineType_Four);
					
			}
		}
		
	}
	
	private void setCapacity(PhysicalMachineType type)
	{
		this.machineType = type;
		

		double cpuCapacityScale = Configuration.getDouble("cpu_capacity_scale");
		double memCapacityScale = Configuration.getDouble("mem_capacity_scale");
		double netCapacityScale = Configuration.getDouble("net_capacity_scale");
	
		double cpuRefCapacity  = Configuration.getDouble("cpu_ref_capacity");
		double memRefCapacity  = Configuration.getDouble("mem_ref_capacity");
		double netRefCapacity  = Configuration.getDouble("net_ref_capacity");

		
		switch (type)
		{
		case PhysicalMachineType_One:
			cpuCapacity     = 0.5  * cpuCapacityScale * cpuRefCapacity;
			memoryCapacity  = 0.25 * memCapacityScale *  memRefCapacity;
			networkCapacity = 1    * netCapacityScale * netRefCapacity;
			powerRate       = cpuCapacity; 
			
			break;
			
		case PhysicalMachineType_Two:
			cpuCapacity     = 0.5 * cpuCapacityScale * cpuRefCapacity;
			memoryCapacity  = 0.5 * memCapacityScale * memRefCapacity;
			networkCapacity = 1   * netCapacityScale * netRefCapacity;
			powerRate       = cpuCapacity;
			
			
			break;
			
		case PhysicalMachineType_Three:
			cpuCapacity     = 0.5 * cpuCapacityScale  * cpuRefCapacity;
			memoryCapacity  = 0.75 * memCapacityScale * memRefCapacity;
			networkCapacity = 1    * netCapacityScale * netRefCapacity;
			powerRate       = cpuCapacity;
			
			
			break;
			
		case PhysicalMachineType_Four:
			cpuCapacity     = 1 * cpuCapacityScale *  cpuRefCapacity;
			memoryCapacity  = 1 * memCapacityScale *  memRefCapacity;
			networkCapacity = 1 * netCapacityScale *  netRefCapacity;
			powerRate       = cpuCapacity;
			
			break;
			
		default: assert(false);
		
		
		
		}
		
		
		
	}
	
	
	
//	public PhysicalMachine(String prefix) {
//
//		this.prefix = prefix;
//		
//		
//		initCapacity();
//		
//		vmList = new VirtualMachinesList();
//		vsm = new VirtualSwitchManager(this);
//	}

	/**
	 * Public call for adding new application to this distributed application
	 * placement protocol
	 * 
	 * @param a
	 *            Application to add
	 */
	public void allocateVM(VirtualMachine a) {
		vmList.add(a);
		a.setPhysicalMachine(this);
		a.incrementValidator();
		localResourcesAllocation();

	}

	/**
	 * Public function for removing application from this distributed
	 * application placement protocol
	 * 
	 * @param a
	 *            Application to remove
	 */
	public void deallocateVM(VirtualMachine a) {
		vmList.remove(a);
		a.decrementValidator();
		localResourcesAllocation();
		
	}

	/**
	 * Read-Only copy of applicationList
	 * 
	 * @param a
	 *            Application to add
	 */
	public VirtualMachinesList vmsList() {

//		return vmList;
		return (VirtualMachinesList) vmList.clone();

	}

	/**
	 * Total number of applications allocated to this protocol at the moment
	 * 
	 * @param a
	 */
	public int appsCount() {
		return vmList.size();
	}

	/**
	 * Total CPU demand from this distributed application placement protocol.
	 * 
	 * @return
	 */
	public double getTotalCPUDemand() {

		// total demand of all Apps plus the demand of the virtual switch manager
		double demandForAllVMs = vmList.totalCPUDemand();
		double demandForVSM    = vsm.getCPUConsumption();
		
		
		return demandForVSM;
	}
	
	public double getTotalMemoryDemand() {

		return vmList.totalMemoryDemand();
	}
	
	public double getTotalNetworkDemand() {

		double sumNetworkDemand = 0.0;
		
		// Iterate all VMs
		for (VirtualMachine vm : vmList)
		{
			// Find the network connection in this VM
			
			for (VirtualMachineLink vmLink : vm.getVmLinks())
			{
				if (vmLink.isConsumeNetworkDemand())
				{
					// 
					sumNetworkDemand += vmLink.getNetworkDemand();
					
				}
				
				
			}
			
		}
		
		
		
		return sumNetworkDemand;
	}
	
	
	
/**
 *  not actually clone because each object will have different capacity
 * this is done to follow PeerSim way of allocationing new node
 */
	public Object clone() {
		
		
		PhysicalMachine proto = new PhysicalMachine(
				this.prefix);
		return proto;
	}
	
	
	/**
	 * actual clone of the Physical machine, this assume that what difference between each machine
	 * is just its type and list of virtual machines
	 * @return
	 */
	public PhysicalMachine actualClone(){
		
		// actual copy of the machines
		
		PhysicalMachine newMachine = new PhysicalMachine(this.prefix);
		
		// set capacity to be the same
		newMachine.setCapacity(this.machineType);
		
		// set Virtual machine list to be the same
		
		newMachine.vmList.addAll(this.vmList);
		
		
		return newMachine;
		
		
	}
	

	
	@Override
	public void nextCycle(Node node, int protocolID) {

		
		
		
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {

	}

}
