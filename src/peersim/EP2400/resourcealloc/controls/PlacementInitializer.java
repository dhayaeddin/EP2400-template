/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.controls;

import peersim.EP2400.resourcealloc.base.VirtualDataCenter;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualDatacentersManager;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;

/**
 * Base class for application placement initializer.
 * @author rerng007
 *
 */
public abstract class PlacementInitializer implements Control{
	// ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------

    /**
     * The number of applications
     * 
     * @config
     */
    private static final String PAR_VDCS_COUNT = "vdcs_count";


    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";

    
	
    
    /**
     * Number of vdcs
     */
    protected int vdcsCount;
    
    
    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    protected final int protocolID;
    
   
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
    public PlacementInitializer(String prefix) {
        protocolID  = Configuration.getPid(prefix + "." + PAR_PROT);
        vdcsCount   = Configuration.getInt(prefix + "." + PAR_VDCS_COUNT);
    }

  
    
	@Override
	public boolean execute()
	{
		// Create virtual data centers according to number of required VDCs
		for(int i=1; i <= vdcsCount; i++)
		{
			VirtualDataCenter vdc = new VirtualDataCenter(i + "");
			VirtualDatacentersManager.getInstance().addVdc(vdc);
			
			//if (i % 100 == 0)
//			System.out.println(String.format("Add VDC id = %s , num tiers = %d, num VMs = %d" ,
//					                           vdc.getID(),
//					                           vdc.getNum_tiers(),
//					                           vdc.getNum_vms_in_VDC()
//					 						));
		}
		return false;
	}
}
