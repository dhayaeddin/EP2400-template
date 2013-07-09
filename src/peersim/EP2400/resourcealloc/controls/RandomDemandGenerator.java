/*
 * Copyright (c) 2010 LCN, EE school, KTH
 *
 */

package peersim.EP2400.resourcealloc.controls;

import peersim.EP2400.resourcealloc.base.VirtualDataCenter;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachinesList;
import peersim.EP2400.resourcealloc.base.VirtualDatacentersManager;
import peersim.EP2400.resourcealloc.base.types.VirtualDataCenterType;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;

/**
 * Class for application demand generator. This generator runs every r_max
 * cycles.
 * 
 * @author rerng007
 * 
 */
public class RandomDemandGenerator extends DemandGenerator {

	private int r_max;
	private static final String PAR_R_MAX = "r_max";

	public RandomDemandGenerator(String prefix) {
		super(prefix);
		r_max = Configuration.getInt(prefix + "." + PAR_R_MAX);

	}

	@Override
	public boolean execute() {

		if (Configuration.getBoolean("debug_mode")) {
			System.out.print("RandomDemandGen - Start changing demand...");

			for (VirtualDataCenter vdc : VirtualDatacentersManager
					.getInstance().getVdcs()) {
				if (vdc.getType() == VirtualDataCenterType.VirtualDataCenterType_Interactive) {
					for (VirtualMachine vm : vdc.getVMs()) {
						double maxCPUDemand = vm.getExpectedCPUMaxDemand();

						double u = CommonState.r.nextDouble();

						double t = CommonState.getTime();
						double sinInput = (2 * Math.PI * t / 86400)
								- (2 * Math.PI * u);
						double nextCPUDemand = maxCPUDemand
								* (1.0 + u * Math.sin(sinInput)) / 2.0;

						vm.setCPUDemand(nextCPUDemand);

					}
				}

			}

			System.out.println("...Finish changing demand");

		}
		return false;
	}

}
