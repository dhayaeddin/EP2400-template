/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.base;

import java.util.ArrayList;
import java.util.List;


/**
 * Singleton class to manage the list of all applications
 * @author Rerngvit Yanggratoke
 *
 */
public class VirtualDatacentersManager {

	/**
	 * Singleton instance
	 */
	private static VirtualDatacentersManager instance;
	
	/**
	 * This list represents A or all applications in the data center.
	 */
	private List<VirtualDataCenter> vdcsList;
	
	/**
	 * Simple Constructor which initialize the instance variables.
	 */
	public VirtualDatacentersManager()
	{
		vdcsList = new ArrayList<VirtualDataCenter>();
	}
	
	/**
	 * Accessor for the Singleton instance.
	 * @return
	 */
	public static VirtualDatacentersManager getInstance()
	{
		if (instance == null)
		{
			instance = new VirtualDatacentersManager();
		}
		
		return instance;
		
		
	}
	
	public void addVdc(VirtualDataCenter vdc)
	{
		this.vdcsList.add(vdc);
		
		
	}
	
	public void removeVdc(VirtualDataCenter vdc)
	{
		this.vdcsList.remove(vdc);
		
		
	}
	

	public VirtualDataCenter[] getVdcs()
	{
		return  this.vdcsList.toArray(new VirtualDataCenter[vdcsList.size()]);
		
		
	}
	
}
