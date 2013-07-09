/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.controls;
import peersim.EP2400.resourcealloc.base.VirtualDataCenter;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualDatacentersManager;
import peersim.EP2400.resourcealloc.base.PhysicalMachine;
import peersim.core.*;

/**
 * This class provides a simple random initializer where applications are randomly allocated to each node
 * @author Rerngvit Yanggratoke
 */
public class RandomPlacementInitializer extends PlacementInitializer{
    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    public RandomPlacementInitializer(String prefix) {
		super(prefix);
	}

	
    public boolean execute() {
    	super.execute();
   
    	
//    	
    	System.out.print(" Random placement initailizer is started...... ");
    	for (VirtualDataCenter vdc : VirtualDatacentersManager.getInstance().getVdcs())
    	{
    		for (VirtualMachine vm: vdc.getVMs())
    		{
	    		int nsize = Network.size();
	    		int nodeIndex =  CommonState.r.nextInt(nsize);
	    		PhysicalMachine p = ((PhysicalMachine) Network.get(nodeIndex).getProtocol(protocolID));
	    		p.allocateVM(vm);
	    		
	    		p.updateLocalDemand();
	    		p.localResourcesAllocation();
	    	//	System.out.println(String.format("VM %s is allocated to server %d", vm.getID(), nodeIndex));
    		}
    		
    	}
    	System.out.println("....finished");
    	
    	return false;
    	
    }

}
