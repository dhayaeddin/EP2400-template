/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.base;

import java.util.HashSet;
import java.util.Set;

import peersim.EP2400.resourcealloc.base.types.VirtualMachineType;
import peersim.config.Configuration;

/**
 * Class to represent an application run in the datacenter
 * 
 * @author Rerngvit Yanggratoke
 * 
 */
public class VirtualMachine {

	/**
	 * Unique Identifier for each Application
	 */
	private String ID;

	public String getID() {
		return ID;
	}

	/**
	 * expected CPU demand for this application
	 */
	private double expectedCPUDemand;

	public double getExpectedCPUDemand() {
		return expectedCPUDemand;
	}

	public void setExpectedCPUDemand(double expectedCPUDemand) {
		this.expectedCPUDemand = expectedCPUDemand;
	}

	private VirtualMachineType vmType;

	public VirtualMachineType getVmType() {
		return vmType;
	}

	public void setVmType(VirtualMachineType vmType) {
		this.vmType = vmType;
	}

	
	public double getExpectedCPUMaxDemand() {
		return expectedCPUMaxDemand;
	}

	public void setExpectedCPUMaxDemand(double expectedCPUMaxDemand) {
		this.expectedCPUMaxDemand = expectedCPUMaxDemand;
	}

	public double getExpectedMemoryMaxDemand() {
		return expectedMemoryMaxDemand;
	}

	public void setExpectedMemoryMaxDemand(double expectedMemoryMaxDemand) {
		this.expectedMemoryMaxDemand = expectedMemoryMaxDemand;
	}

	private double expectedCPUMaxDemand;
	
	private double expectedMemoryMaxDemand;
	
	/**
	 * CPU demand of an application
	 */
	private double CPUDemand;

	/**
	 * Amount of CPU limit that will be allocated to
	 */
	private double CPULimit;

	private double CPUAllocation;

	/**
	 * memory demand of an application
	 */
	private double MemoryDemand;

	private double MemoryLimit;

	private double MemoryAllocation;

	private VirtualDataCenter vdc;
	
	private Set<VirtualMachineLink> vmLinkSet;

	public VirtualDataCenter getVdc() {
		return vdc;
	}

	public void setVdc(VirtualDataCenter vdc) {
		this.vdc = vdc;
	}

	public double getCPULimit() {
		return CPULimit;
	}

	public void setCPULimit(double cPULimit) {
		CPULimit = cPULimit;
	}

	public double getCPUAllocation() {
		return CPUAllocation;
	}

	public void setCPUAllocation(double cPUAllocation) {
		CPUAllocation = cPUAllocation;
	}

	public double getMemoryLimit() {
		return MemoryLimit;
	}

	public void setMemoryLimit(double memoryLimit) {
		MemoryLimit = memoryLimit;
	}

	public double getMemoryAllocation() {
		return MemoryAllocation;
	}

	public void setMemoryAllocation(double memoryAllocation) {
		MemoryAllocation = memoryAllocation;
	}

	/**
	 * The protocol that this application is allocated to
	 */
	private PhysicalMachine distPlacementProtocol;

	/**
	 * Accessor for currently allocated distPlacementProtocol
	 * 
	 * @return
	 */
	public PhysicalMachine getPhysicalMachine() {
		return distPlacementProtocol;
	}

	/**
	 * Getter for currently allocated distPlacementProtocol
	 * 
	 * @return
	 */
	public void setPhysicalMachine(PhysicalMachine distPlacementProtocol) {
		this.distPlacementProtocol = distPlacementProtocol;
	}

	/**
	 * A simple validator whether the applications were allocated twice or not
	 * allocated at all
	 */
	private int allocationCountValidator;

	/**
	 * Accessor to allocationCountValidator
	 * 
	 * @return
	 */
	public int getAllocationCountValidator() {
		return allocationCountValidator;
	}

	/**
	 * Increment the validator
	 */
	public void incrementValidator() {
		allocationCountValidator++;
	}

	/**
	 * decrement the validator
	 */
	public void decrementValidator() {
		allocationCountValidator--;
	}

	/**
	 * Accessor for current CPU demand
	 * 
	 * @return
	 */
	public double getCPUDemand() {
		return CPUDemand;
	}
	
	public double getVMCPUUtilization()
	{
		return CPUDemand / CPULimit;
		
	}
	

	/**
	 * Setter for the CPU demand
	 * 
	 * @param cpuDemand
	 */
	public void setCPUDemand(double cpuDemand) {
		// update the value in the protocol

		CPUDemand = cpuDemand;
		
		for (VirtualMachineLink vmLink: vmLinkSet)
		{

			if (vmLink.isConsumeNetworkDemand())
			{
				double maxCPUUtilization = getVMCPUUtilization();
				double otherCPUUtilization = vmLink.getOtherVirtualMachine(this).getVMCPUUtilization();
				
				if (otherCPUUtilization > maxCPUUtilization)
					maxCPUUtilization = otherCPUUtilization;
				
				vmLink.setNetworkDemand(maxCPUUtilization * vmLink.getNetworkLimit());
				
			}
			else
				vmLink.setNetworkDemand(0);
			
		}
		
		
	}

	/**
	 * Accessor for current memory demand
	 * 
	 * @return
	 */
	public double getMemoryDemand() {
		return MemoryDemand;
	}

	/**
	 * Setter for the memory demand
	 */

	public void setMemoryDemand(double memoryDemand) {
		MemoryDemand = memoryDemand;
	}

	/**
	 * check function whether an application pass the constraint.
	 * 
	 * @return
	 */
	public boolean isPassConstraints() {
		// it is valid if and only if the allocationCountValidator is exactly 1
		// In other words, there is exactly one machine allocate this
		// application.
		return allocationCountValidator == 1;

	}

	/**
	 * Constructor based on input CPU demand
	 * 
	 * @param CPUDemand
	 *            cpu demand for this application instance
	 */
	public VirtualMachine(String ID, VirtualMachineType vmType) {
		this.ID = ID;
		this.distPlacementProtocol = null;
		this.allocationCountValidator = 0;

		this.vmLinkSet  = new HashSet<VirtualMachineLink>();
		this.vmType = vmType;
		this.setParametersByType(vmType);

	}

	private void setParametersByType(VirtualMachineType vmType) {

		double cpuRefCapacity =  Configuration.getDouble("cpu_ref_capacity");
		double memRefCapacity =  Configuration.getDouble("mem_ref_capacity");
		
		switch (vmType) {
		case VirtualMachineType_Large:
			CPULimit    = 8 *  cpuRefCapacity;
			MemoryLimit = 8 *  memRefCapacity;
			
			
			break;

		case VirtualMachineType_HighCPU:
			CPULimit    = 8 *  cpuRefCapacity;
			MemoryLimit = 2 *  memRefCapacity;
			
			break;

		case VirtualMachineType_HighMemory:
			CPULimit    = 2 *  cpuRefCapacity;
			MemoryLimit = 8 *  memRefCapacity;
			
			
			break;

		case VirutalMachineType_Small:
			CPULimit    = 0.5 *  cpuRefCapacity;
			MemoryLimit = 0.5 *  memRefCapacity;
			
			break;

		default: assert(false);
		}
		
	    expectedCPUMaxDemand    = 1.0 * CPULimit;
		expectedMemoryMaxDemand = 1.0 * MemoryLimit;
		
		setCPUDemand(expectedCPUMaxDemand);
		setMemoryDemand(expectedMemoryMaxDemand);

	}
	
	
	public void addLink(VirtualMachineLink vmLink)
	{
		vmLinkSet.add(vmLink);		
	}
	
	public VirtualMachineLink[] getVmLinks()
	{
		return vmLinkSet.toArray(new VirtualMachineLink[vmLinkSet.size()]);
	}

}
