package peersim.EP2400.resourcealloc.tasks;

import java.util.Set;

import peersim.EP2400.resourcealloc.base.ResourcesTriple;
import peersim.EP2400.resourcealloc.base.PhysicalMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachineLink;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.util.IncrementalStats;

public class ResourceAllocationLogic {

	
	
	public static ResourcesTriple v(PhysicalMachine p) {

		double cpuResource = p.getCpuCapacity() - p.getTotalCPUAllocation();
		double memResource = p.getMemoryCapacity() - p.getTotalMemoryAllocation();
		double networkResourcce = p.getNetworkCapacity()
				- p.getTotalNetworkAllocation();

		ResourcesTriple out = new ResourcesTriple(cpuResource, memResource,
				networkResourcce);

		return out;
	}

	// compute delta
	public static ResourcesTriple delta(PhysicalMachine p) {

		ResourcesTriple v = ResourceAllocationLogic.v(p);
		ResourcesTriple delta = v.abs().minus(v).divideConstant(2);

		return delta;

	}
	
	public static ResourcesTriple[]  delta_m(PhysicalMachine src, PhysicalMachine dest, VirtualMachine m) {

		assert(src.equals(m.getPhysicalMachine()));
		assert(src.vmsList().contains(m));
		assert(!dest.vmsList().contains(m));

		ResourcesTriple[] delta_out = new ResourcesTriple[2];
		
		// move m from src to dest
		src.deallocateVM(m);
		dest.allocateVM(m);
		
		delta_out[0]   = delta(src);
		delta_out[1]   = delta(dest);
		
		// move m back
		src.allocateVM(m);
		dest.deallocateVM(m);
		
		return delta_out;

	}
	
	

	public static ResourcesTriple bigDelta(PhysicalMachine p) {

		ResourcesTriple v = ResourceAllocationLogic.v(p);

		ResourcesTriple delta = v.abs().plus(v).divideConstant(2);

		return delta;

	}
	
	public static ResourcesTriple[] bigDelta_m(PhysicalMachine src, PhysicalMachine dest, VirtualMachine m) {
		
		assert(src.equals(m.getPhysicalMachine()));
		assert(src.vmsList().contains(m));
		assert(!dest.vmsList().contains(m));
		
		ResourcesTriple[] big_delta_out = new ResourcesTriple[2];
		
		// move m from src to dest
		src.deallocateVM(m);
		dest.allocateVM(m);
		
		big_delta_out[0]   = bigDelta(src);
		big_delta_out[1]   = bigDelta(dest);

		// move m back
		src.allocateVM(m);
		dest.deallocateVM(m);
		
		return big_delta_out;

	}
	
	
	/**
	 * This method check whether this physical machine has free resources to accommodate virtual machine m
	 * @param p
	 * @param m
	 * @return
	 */
	public static boolean isVMMovable(PhysicalMachine src, PhysicalMachine dest, VirtualMachine m)
	{
		assert(src.equals(m.getPhysicalMachine()));	
		assert(src.vmsList().contains(m));		
		assert(!dest.vmsList().contains(m));

		// compute the resource to for VM live migration here
		// we consider that all live migration will be transfered in two minutes and
		// the data dirty rate will be twice as memory
		double memRefCapacity =  Configuration.getDouble("mem_ref_capacity");
		double VMMemLimitInMb   = m.getMemoryLimit() * 1000 * 8 / memRefCapacity;
		double network_OneMin_DoubleRamDirty = VMMemLimitInMb * 2 / 60;
		
		double liveNetDemand  = network_OneMin_DoubleRamDirty;
		
		double cpuConsumeSrc =    m.getMemoryLimit();
		
		
		ResourcesTriple liveConsumeSrc    = new ResourcesTriple(cpuConsumeSrc, 0,   liveNetDemand);
		ResourcesTriple liveConsumeDest   = new ResourcesTriple(0, 0,   liveNetDemand);
		
		// Move from Src to Dest
		src.deallocateVM(m);
		dest.allocateVM(m);
		
		
		// compute the available resources of the VM here whether all of them is greater than 0
		ResourcesTriple left_dest  = v(dest).minus(liveConsumeDest);
		ResourcesTriple left_src   = v(src).minus(liveConsumeSrc);

		boolean isVMMovable = left_dest.isAllNonZero() && left_src.isAllNonZero();
		
		// Move back from Src to Dest
		src.allocateVM(m);
		dest.deallocateVM(m);
		
		return isVMMovable;
	}


	

	/**
	 * Compute C_m
	 * @param dest
	 * @param m
	 * @return
	 */
	public static double[] C_m(PhysicalMachine src, PhysicalMachine dest, VirtualMachine m) {
		assert(m.getPhysicalMachine().equals(src));
		assert(!dest.vmsList().contains(m));		
		ResourcesTriple[] bigDelta_m_out  = bigDelta_m(src, dest, m);		
		
		
		double[] c_m_out  = new double[2];

		// source
		ResourcesTriple bigDelta_n_src    = bigDelta(src);
		ResourcesTriple bigDelta_n_m_src  = bigDelta_m_out[0];
		c_m_out[0]    = bigDelta_n_src.minus(bigDelta_n_m_src).pairWiseDivision(bigDelta_n_src).infiniNorm();	
		
		// dest
		ResourcesTriple    bigDelta_n_m_dest  = bigDelta_m_out[1];
		ResourcesTriple    bigDelta_n_dest    = bigDelta(dest);
		
		c_m_out[1] = bigDelta_n_dest.minus(bigDelta_n_m_dest).pairWiseDivision(bigDelta_n_dest).infiniNorm();				
		return c_m_out;
	}
	
	
	
	/**
	 * The search should try to pick something that maximize this value
	 * @param src
	 * @param dest
	 * @param m
	 * @return
	 */
	public static double valueConsideredForFixOverload(PhysicalMachine src, PhysicalMachine dest, VirtualMachine m)
	{
		
		ResourcesTriple delta_src   = delta(src);
		ResourcesTriple delta_src_m = delta_m(src, dest, m)[0];
		
		double c_m_out[] = C_m(src, dest, m);
		double max_cost   = Math.max(c_m_out[0], c_m_out[1]);
		
		double val =  delta_src.minus(delta_src_m).twoNorm() / max_cost;
		return val;
	}
	
	
	private static double f_EN(PhysicalMachine node_i, PhysicalMachine node_j)
	{
		double i_max_util = Math.max(node_i.getCPUUtilization(), Math.max(node_i.getMemoryUtilization(), node_i.getNetworkUtilization()));
		double i_power_term = node_i.getPowerRate() * Math.sqrt(i_max_util);
		
		double j_max_util = Math.max(node_j.getCPUUtilization(), Math.max(node_j.getMemoryUtilization(), node_j.getNetworkUtilization()));
		double j_power_term = node_j.getPowerRate() * Math.sqrt(j_max_util);
			
		double	obj_function = i_power_term + j_power_term;
			
		return obj_function;
			
	}
	
	private static double f_LB(PhysicalMachine node_i, PhysicalMachine node_j)
	{
		
		/**
		 * Two norm codes
		 */		
		double sum_cpu_util_squre = Math.pow(node_i.getCPUUtilization() , 2)    + Math.pow(node_j.getCPUUtilization() , 2);
		double sum_mem_util_squre = Math.pow(node_i.getMemoryUtilization() , 2) + Math.pow(node_j.getMemoryUtilization() , 2);
		double sum_net_util_squre = Math.pow(node_i.getNetworkUtilization(), 2) + Math.pow(node_j.getNetworkUtilization() , 2);
		
		double obj_function = Math.sqrt(sum_cpu_util_squre) + Math.sqrt(sum_mem_util_squre) +  Math.sqrt(sum_net_util_squre);
		return obj_function;
		
		
		/**
		 * Old variance code
		 */
		
		// compute variance of CPU utilization between the two VMs
//		IncrementalStats cpuUtilstat = new IncrementalStats();
//		IncrementalStats memoryUtilStat = new IncrementalStats();
//		IncrementalStats networkUtilStat = new IncrementalStats();
//		
//		cpuUtilstat.add(node_i.getCPUUtilization());
//		cpuUtilstat.add(node_j.getCPUUtilization());
//		
//		
//		memoryUtilStat.add(node_i.getMemoryUtilization());
//		memoryUtilStat.add(node_j.getMemoryUtilization());
//		
//		networkUtilStat.add(node_i.getNetworkUtilization());
//		networkUtilStat.add(node_j.getNetworkUtilization());
//		
//		
//		return cpuUtilstat.getVar() + memoryUtilStat.getVar() + networkUtilStat.getVar();
		
		
	}
	
	
	public static double f(PhysicalMachine src, PhysicalMachine dest)
	{
		String objective = Configuration.getString("performance_objective");
		if (objective.equals("LB") || objective.equals("EN"))
		{
			return ResourceAllocationLogic.f_LB(src, dest);
		}
		else
		{
			return ResourceAllocationLogic.f_EN(src, dest);
			
		}
		
		
		
		
	}

	public static double f_m(PhysicalMachine src, PhysicalMachine dest,
			VirtualMachine m) {
		assert (src.equals(m.getPhysicalMachine()));
		assert (src.vmsList().contains(m));
		assert (!dest.vmsList().contains(m));

		// Move from Src to Dest
		src.deallocateVM(m);
		dest.allocateVM(m);

		double output = f(src, dest);
		
		// Move back from Src to Dest
		src.allocateVM(m);
		dest.deallocateVM(m);

		return output;
	}
	
	
	public static double valConsiderToAchieveGoal(PhysicalMachine src, PhysicalMachine dest,
			VirtualMachine m)
	{
		
		double f = f(src, dest);
		double f_m = f_m(src, dest, m);
		
		
		double[] c_m_out = C_m(src, dest, m);
		double max_cost   = Math.max(c_m_out[0], c_m_out[1]);
		
		double val = (f - f_m) / max_cost;
		return val;
		
	}
	
	private static void localResourceAllocation_LB_EN(PhysicalMachine machine)
	{
		for (VirtualMachine vm : machine.vmsList())
		{
			vm.setCPUAllocation(vm.getCPUDemand());
			vm.setMemoryAllocation(vm.getMemoryDemand());
			
			
			for (VirtualMachineLink vmLink: vm.getVmLinks())
			{
				vmLink.setNetworkAllocation(vmLink.getNetworkDemand());
			}
			
		}
	}
	
	public static void localResourceAllocation(PhysicalMachine machine)
	{
		String objective = Configuration.getString("performance_objective");

		if (objective.equals("LB") || objective.equals("EN"))
		{
			localResourceAllocation_LB_EN(machine);
		}
		else
		{

			//TODO add for fair resource allocation here
			
		}
		
	}
	
	
}
