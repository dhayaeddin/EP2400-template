package peersim.EP2400.resourcealloc.tasks;

import java.io.File;

import peersim.EP2400.resourcealloc.base.PhysicalMachine;
import peersim.EP2400.resourcealloc.base.VirtualDataCenter;
import peersim.EP2400.resourcealloc.base.VirtualDatacentersManager;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachineLink;
import peersim.EP2400.resourcealloc.util.FileIO;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

public class PerformanceObserver implements Control {

	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_PROT = "protocol";

	/**
	 * The number of applications
	 * 
	 * @config
	 */
	private static final String PAR_VDCSCOUNT = "vdcs_count";

	private static final String PAR_R_MAX = "r_max";

	/** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
	private final int pid;

	private final String prefix;

	/**
	 * Constant r_max in the simulation
	 */
	private final int r_max;


	private String outputFolder;

	private String outCMNLFFile;
	private String outSLAFile;
	private String outEffFile;
	
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine.
	 * 
	 * @param prefix
	 *            the configuration prefix identifier for this class.
	 */
	public PerformanceObserver(String prefix) {
		this.prefix = prefix;
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		r_max = Configuration.getInt(prefix + "." + PAR_R_MAX);

		outputFolder = Configuration.getString("sim_out_folder");
		
		File outputFolderFile = new File(outputFolder);
		if (!outputFolderFile.exists())
			outputFolderFile.mkdir();
		
		outCMNLFFile = outputFolder + "/cmnlf.log";
		outSLAFile   = outputFolder + "/sla.log";
		outEffFile   = outputFolder + "/eff.log";
		
		
		FileIO.delete(outCMNLFFile);
		FileIO.delete(outSLAFile);
		FileIO.delete(outEffFile);
		
	}
	
	private void print_effectiveness_LB()
	{
		double sum_cpu_util_squre = 0;
		double sum_mem_util_squre = 0;
		double sum_net_util_squre = 0;
		
		for (int i = 0; i < Network.size(); i++) {
			PhysicalMachine p = (PhysicalMachine) Network
					.get(i).getProtocol(pid);
			
			sum_cpu_util_squre += Math.pow(p.getCPUUtilization() , 2);
			sum_mem_util_squre += Math.pow(p.getMemoryUtilization(), 2);
			sum_net_util_squre += Math.pow(p.getNetworkUtilization(), 2);
			
		}
		
		double obj_function = Math.sqrt(sum_cpu_util_squre) + Math.sqrt(sum_mem_util_squre) +  Math.sqrt(sum_net_util_squre);
		
		double target = 0.0;
		
		String printedResult = String
				.format("LB Obj: Distance = %.2f, Obj = %.2f, Target = %.2f, SumCPU^2 = %.2f, SumMem^2 = %.2f, SumNet^2 = %.2f",
						obj_function - target,
						obj_function,
						target,
						sum_cpu_util_squre,
						sum_mem_util_squre,
						sum_net_util_squre
						);
		
		System.out.println(printedResult);
		
		String writeOutResult = String
				.format("%d %.4f %.4f %.4f %.4f %.4f %.4f",
						CommonState.getTime(),
						obj_function - target,
						obj_function,
						target,
						sum_cpu_util_squre,
						sum_mem_util_squre,
						sum_net_util_squre
						);
		
		FileIO.write(writeOutResult, outEffFile);
		
		
	}
	
	private void print_effectiveness_EN()
	{
		double sum_power_term = 0.0;
		double num_active_servers = 0;
		
		for (int i = 0; i < Network.size(); i++) {
			PhysicalMachine p = (PhysicalMachine) Network
					.get(i).getProtocol(pid);
			
			double p_max_util = Math.max(p.getCPUUtilization(), Math.max(p.getMemoryUtilization(), p.getNetworkUtilization()));
			double p_power_term = p.getPowerRate() * Math.sqrt(p_max_util);
			
			
			if (p.getCPUUtilization() > 0 || p.getMemoryUtilization() > 0 || p.getNetworkUtilization() > 0)
				num_active_servers++;
			
			sum_power_term += p_power_term;
			
			
			
		}
		
		double obj_function = sum_power_term;
		
		double target = 0.0;
		
		double fraction_active_servers = num_active_servers / Network.size();
		
		String printedResult = String
				.format("Ener Obj: Distance = %.2f, Obj = %.2f, Target = %.2f, R = %.2f",
						obj_function - target,
						obj_function,
						target,
						fraction_active_servers
						);
		
		System.out.println(printedResult);
		
		String writeOutResult = String
				.format("%d %.4f %.4f %.4f %.4f",
						CommonState.getTime(),
						obj_function - target,
						obj_function,
						target,
						fraction_active_servers
						);
		
		FileIO.write(writeOutResult, outEffFile);
		
		
	}
	
	
	private void printEffectiveness()
	{
		String objective = Configuration.getString("performance_objective");
		if (objective.equals("LB") )
		{
			print_effectiveness_LB();
		}
		else if (objective.equals("EN"))
		{
			print_effectiveness_EN();
			
		}
		else if (objective.equals("FA"))
		{
			
			
		}
		
		
		
		
	}

	private void printCMNLF()
	{
		
		
		double totalCPUDemand = 0, totalMemoryDemand =0, totalNetworkDemand =0;
		
		for (VirtualDataCenter vdc : VirtualDatacentersManager.getInstance().getVdcs())
		{
			
			for (VirtualMachine vm: vdc.getVMs())
			{
				totalCPUDemand    += vm.getCPUDemand();
				totalMemoryDemand += vm.getMemoryDemand();
				
				for (VirtualMachineLink vmLink: vm.getVmLinks())
				{
					// not remove duplication
					//if (vmLink.isConsumeNetworkDemand())
					totalNetworkDemand += vmLink.getNetworkDemand();
					
				}
				
			}
			
			
			
		}
		
		double totalCPUCapacity =0, totalMemoryCapacity = 0, totalNetworkCapacity = 0;

		for (int i = 0; i < Network.size(); i++) {
			PhysicalMachine p = (PhysicalMachine) Network
					.get(i).getProtocol(pid);
			
			totalCPUCapacity += p.getCpuCapacity();
			totalMemoryCapacity += p.getMemoryCapacity();
			totalNetworkCapacity += p.getNetworkCapacity();
		}
		
		
		
		String printedResult = String
				.format("Load factors : CLF = %f, MLF = %f, NLF = %f",
						totalCPUDemand / totalCPUCapacity,
						totalMemoryDemand / totalMemoryCapacity,
						totalNetworkDemand / totalNetworkCapacity);
		System.out.println(printedResult);
		
		String writeOutResult = String
				.format("%d %.4f %.4f %.4f %.2f %.2f %.2f %.2f %.2f %.2f\n",
						CommonState.getTime(),
						totalCPUDemand / totalCPUCapacity,
						totalMemoryDemand / totalMemoryCapacity,
						totalNetworkDemand / totalNetworkCapacity,
						totalCPUDemand,
						totalMemoryDemand,
						totalNetworkDemand,
						totalCPUCapacity,
						totalMemoryCapacity,
						totalNetworkCapacity);
		FileIO.append(writeOutResult, outCMNLFFile);
		
//		printedResult = String
//				.format("Demand : CPUDemand = %f, Memory Demand = %f, Network Demand = %f",
//						totalCPUDemand,
//						totalMemoryDemand,
//						totalNetworkDemand);
//		System.out.println(printedResult);
//		
//		printedResult = String
//				.format("Capacity : CPUCapacity = %f, MemoryCapacity = %f, NetworkCapacity = %f",
//						totalCPUCapacity,
//						totalMemoryCapacity,
//						totalNetworkCapacity);
//		System.out.println(printedResult);
			
	}
	
	private void printSLAVioloation()
	{
		IncrementalStats serversCPUUtilStats = new IncrementalStats();
		IncrementalStats serversMemoryUtilStats = new IncrementalStats();
		IncrementalStats serversNetworkUtilStats = new IncrementalStats();
		
		
		
		int num_busy_servers = 0;
		int num_CPU_overloaded_servers = 0;
		int num_Memory_overloaded_servers = 0;
		int num_Network_overloaded_servers = 0;
		int num_servers_SLA_violated = 0;
		
		final int len = Network.size();
		for (int i = 0; i < len; i++) {
			PhysicalMachine p = (PhysicalMachine) Network
					.get(i).getProtocol(pid);
			serversCPUUtilStats.add(p.getCPUUtilization());
			serversMemoryUtilStats.add(p.getMemoryUtilization());
			serversNetworkUtilStats.add(p.getNetworkUtilization());
			
			if (p.appsCount() > 0)
				num_busy_servers++;
			
			if (p.getCPUUtilization() > 1)
				num_CPU_overloaded_servers++;
				

			if (p.getMemoryUtilization() > 1)
				num_Memory_overloaded_servers++;
			

			if (p.getNetworkUtilization() > 1)
				num_Network_overloaded_servers++;
		
			if (p.getCPUUtilization() > 1 || p.getMemoryUtilization() > 1 || p.getNetworkUtilization() > 1)
				num_servers_SLA_violated++;
			
		}

		
		double resource_consumption    = ((double) num_busy_servers) / len;
		double fraction_cpu_overloaded =  ((double) num_CPU_overloaded_servers) / len;
		double fraction_mem_overloaded =  ((double) num_Memory_overloaded_servers) / len;
		double fraction_net_overloaded =  ((double) num_Network_overloaded_servers) / len;
		double fraction_sla_violated   =  ((double) num_servers_SLA_violated) / len;
		
		
		double covCPUUtil                    = Math.sqrt(serversCPUUtilStats.getVar())     / serversCPUUtilStats.getAverage();
		double covMemUtil                    = Math.sqrt(serversMemoryUtilStats.getVar())  / serversMemoryUtilStats.getAverage();
		double covNetworkUtil                = Math.sqrt(serversNetworkUtilStats.getVar()) / serversNetworkUtilStats.getAverage();

		
		
		
		String printedResult = String
		.format("Cov : CoV-CPU = %f, Cov-Mem = %f, Cov-Net = %f,  R = %f",
				covCPUUtil,
				covMemUtil,
				covNetworkUtil,
				resource_consumption);
		System.out.println(printedResult);
		
		printedResult = String
		.format("SLAs : CPU-Overload = %f, Mem-Overload = %f, Net-Overload = %f, SLA Violated = %f",
				fraction_cpu_overloaded,
				fraction_mem_overloaded,
				fraction_net_overloaded,
				fraction_sla_violated
				);
		System.out.println(printedResult);
		
		String writeOutResult = String
				.format("%d %.4f %.4f %.4f %.4f\n",
						CommonState.getTime(),
						fraction_sla_violated,
						fraction_cpu_overloaded,
						fraction_mem_overloaded,
						fraction_net_overloaded);
		FileIO.append(writeOutResult, outSLAFile);
		
		
	}
	
	@Override
	public boolean execute() {

		printSLAVioloation();
		printCMNLF();
		printEffectiveness();

		return false;
	}

}
