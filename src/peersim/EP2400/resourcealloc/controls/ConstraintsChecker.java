/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.controls;

import peersim.EP2400.resourcealloc.base.VirtualDataCenter;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachinesList;
import peersim.EP2400.resourcealloc.base.VirtualDatacentersManager;
import peersim.core.Control;

/**
 * Simple constraint checker that runs periodically. It checks whether 
 * the implemented protocol violates the constraints or not.
 * @author Rerngvit Yanggratoke
 *
 */
public class ConstraintsChecker  implements Control{

	public ConstraintsChecker(String prefix) {
		  
	   }
	@Override
	public boolean execute()
	{
		
		System.out.print("Constraints checker is started .....");
		for (VirtualDataCenter vdc: VirtualDatacentersManager.getInstance().getVdcs())
		{
			for( VirtualMachine a : vdc.getVMs())
			{
				if (!a.isPassConstraints())
				{
					String terminateMessage = String.format("Application %d  Constraint is invalid. The application is allocated to %d machines simultaneously.", 
				    		a.getID(), a.getAllocationCountValidator());
				    System.out.println(terminateMessage);
			    	System.out.println(" Terminating now");
			    	System.exit(1);
					
				}
				
			}
			
		}
		
		System.out.println("...finished");
		
		
		
		
		return false;
	}
}
