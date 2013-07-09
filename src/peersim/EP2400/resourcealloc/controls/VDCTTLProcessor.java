/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.controls;

import java.util.HashSet;
import java.util.Set;

import peersim.EP2400.resourcealloc.base.VirtualDataCenter;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachinesList;
import peersim.EP2400.resourcealloc.base.VirtualDatacentersManager;
import peersim.EP2400.resourcealloc.base.types.VirtualDataCenterType;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;

/**
 * Class for application demand generator. This generator runs every r_max cycles.
 * @author rerng007
 *
 */
public class VDCTTLProcessor extends DemandGenerator {

	private int r_max;
	private static final String PAR_R_MAX = "r_max";
	
	public VDCTTLProcessor(String prefix) {
		super(prefix);
		r_max = Configuration.getInt(prefix + "."
				+ PAR_R_MAX);
		
	}

	
	
	@Override
	public boolean execute() {

		System.out.print("VDC TTL Processor - Start...");
		
		Set<VirtualDataCenter> tobeRemoved = new HashSet<VirtualDataCenter>();  	
    	for (VirtualDataCenter vdc : VirtualDatacentersManager.getInstance().getVdcs())
    	{
    		if (vdc.getType()  == VirtualDataCenterType.VirtualDataCenterType_ComputationIntensive)
    		{
    			long curTTL = vdc.getTimeToLive();
    			
    			int step    = Configuration.getInt("time_step_per_cycle");
    			
    			
    			long newTTL = curTTL - step;
    			vdc.setTimeToLive(newTTL);
    			
    			if (newTTL <= 0)
    			{
    				// remove VDC here
    				tobeRemoved.add(vdc);
    				
    			}
    			
    			
    			
    		}
    		
    	}
    	
    	int vdc_removed  = 0;
    	for (VirtualDataCenter vdc : tobeRemoved)
    	{
    		System.out.println("Expiring VDC - ID = " + vdc.getID());
    		// remove each vdc here
    		
    		for (VirtualMachine vm : vdc.getVMs())
    		{
    			vm.getPhysicalMachine().deallocateVM(vm);
    			
    		}
    		
    		vdc_removed++;
    		VirtualDatacentersManager.getInstance().removeVdc(vdc);
    	}
    	
		
		System.out.println("...Finish - total remove = " + vdc_removed);
		
		
		return false;
	}

}
