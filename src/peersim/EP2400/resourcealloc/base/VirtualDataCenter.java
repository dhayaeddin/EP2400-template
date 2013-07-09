package peersim.EP2400.resourcealloc.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peersim.EP2400.resourcealloc.base.types.VirtualDataCenterType;
import peersim.EP2400.resourcealloc.base.types.VirtualMachineLinkType;
import peersim.EP2400.resourcealloc.base.types.VirtualMachineType;
import peersim.core.CommonState;

public class VirtualDataCenter {

	private Set<VirtualMachine> vmset;
	private String ID;

	private int num_tiers;
	private int num_vms_in_VDC;
	private int num_vms_in_tier[];

	private List<List<VirtualMachine>> vms_tiers;
	private final VirtualDataCenterType type;
	
	private long timeToLive;
	
	public long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public VirtualDataCenterType getType() {
		return type;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	
	/**
	 * Init Time to live of a VDC according to power law distribution
	 */
	private void initTimeToLive()
	{
		long secondsInTimeUnit = 1000000;
		
		long minsInTimeUnit  = 1 * 60 * secondsInTimeUnit;
		long maxsInTimeUnit  = 5 * 60 * secondsInTimeUnit;
		
//		long minsInTimeUnit  = 10 * 60 * secondsInTimeUnit;
//		long maxsInTimeUnit   = 24 * 60 * secondsInTimeUnit;
		
		int exponent = 2;
		timeToLive = samplePowerLaw(exponent, minsInTimeUnit, maxsInTimeUnit);
		
		
		
	}
	/**
	 * Contructor based on the input ID
	 * 
	 * @param ID
	 */
	public VirtualDataCenter(String ID) {
		vmset = new HashSet<VirtualMachine>();

		this.ID = ID;
		
		// half probability being interactive
//		if (CommonState.r.nextDouble() <= 0.5)
		this.type = VirtualDataCenterType.VirtualDataCenterType_Interactive;
//		else
//			this.type = VirtualDataCenterType.VirtualDataCenterType_ComputationIntensive;

		if (type == VirtualDataCenterType.VirtualDataCenterType_ComputationIntensive)
			initTimeToLive();
		
		// sample a number from Zipf distribution
		
		int sample_num_vms = sampleNumVMsInVDC();
		num_vms_in_VDC = Math.max(num_tiers, sample_num_vms);
		
		
		num_tiers = sampleNumTiers();

		assert(num_tiers > 0);
		vms_tiers = new ArrayList<List<VirtualMachine>>();

		for (int i = 0; i < num_tiers; i++) {
			List<VirtualMachine> vms_this_tier = new ArrayList<VirtualMachine>();

			vms_tiers.add(vms_this_tier);
		}

		initVDC();
	}

	public int getNum_tiers() {
		return num_tiers;
	}


	public int getNum_vms_in_VDC() {
		return num_vms_in_VDC;
	}
	
	public VirtualMachine[] getVMs()
	{
		return   vmset.toArray(new VirtualMachine[vmset.size()]);
		
	}

	private long samplePowerLaw(int n, long min_out, long max_out)
	{
		double random = CommonState.r.nextDouble();

		double first_in_bracket = (Math.pow(max_out, n + 1) - Math.pow(min_out ,n + 1))	* random;
		double second_in_bracket = Math.pow(min_out , n + 1);

		double power_of_bracket = (1.0 / (n + 1));
		long   out = (long) Math.pow(first_in_bracket + second_in_bracket,
				power_of_bracket);
		
		return out;
	}

	/**
	 * Sample number of VMs in VDC from a power-law distribution
	 * 
	 * @return
	 */
	private int sampleNumVMsInVDC() {
		int n = 2;
		int min_vms = 1;
		int max_vms = 30;


		return (int) samplePowerLaw(n, min_vms,  max_vms);
	}

	private int sampleNumTiers() {
		
		if (num_vms_in_VDC == 1) return 1;
		
		if (num_vms_in_VDC == 2) 
		{
			if( CommonState.r.nextDouble() >= 0.5) return 1;
			else return 2;
			
		}

		
		double random = CommonState.r.nextDouble();
		if (random <= 0.33)
			return 1;
		else if (random <= 0.66)
			return 2;
		else
			return 3;

	}

	private VirtualMachineLinkType sampleLinkType(VirtualMachine srcVm,
			VirtualMachine destVM) {
		if (srcVm.getVmType() == VirtualMachineType.VirutalMachineType_Small
				|| destVM.getVmType() == VirtualMachineType.VirutalMachineType_Small)
			return VirtualMachineLinkType.VirtualMachineLinkType_Small;

		double random = CommonState.r.nextDouble();

		if (random <= 10/11)
			return VirtualMachineLinkType.VirtualMachineLinkType_Medium;
		else
			return VirtualMachineLinkType.VirtualMachineLinkType_Large;
	}

	private VirtualMachineType sampleVMType() {
		double random = CommonState.r.nextDouble();

		// Target
		// VirtualMachineType_Large p = 1/25
		// VirtualMachineType_HighCPU p = 4/25
		// VirtualMachineType_HighMemory p = 4/25
		// VirtualMachineType_Small p = 16/25

		if (random <= 1 / 25)
			return VirtualMachineType.VirtualMachineType_Large;
		else if (random <= 5 / 25)
			return VirtualMachineType.VirtualMachineType_HighCPU;
		else if (random <= 9 / 25)
			return VirtualMachineType.VirtualMachineType_HighMemory;

		else
			return VirtualMachineType.VirutalMachineType_Small;

	}

	private void allocateNumVMsInTiers() {
		
		assert(num_vms_in_VDC >= num_tiers);
		num_vms_in_tier = new int[num_tiers];

		
		int assigned_vms = 0;
		// assign number of VMs from tier 0 - tier num_tiers - 1
		for (int i = 0; i < num_tiers - 1; i++) {
			int random_num_vms = (int) (CommonState.r.nextDouble() * num_vms_in_VDC);
			int reserved_vms = num_tiers - (i + 1);
			int max_num_vms = (num_vms_in_VDC - assigned_vms) - reserved_vms;
			num_vms_in_tier[i] = Math.min(
					Math.max(random_num_vms, max_num_vms), 1);
			assigned_vms += num_vms_in_tier[i];

		}
		// the last tier take all the VMs
		num_vms_in_tier[num_tiers - 1] = num_vms_in_VDC - assigned_vms;
		
		// check
		for (int i = 0; i < num_tiers; i++)
		{
			assert(num_vms_in_tier[i] > 0);
			
			//assert(num_vms_in_tier[i] == vms_tiers.get(i).size());
			
		}
		

	}

	private void addActualVMsToEachTier() {
		// sample VMs in each tier
		for (int i = 0; i < num_tiers; i++) {
			// tier i = 0 ... num_tiers - 1
			// the VMs should all have the same type
			VirtualMachineType type = sampleVMType();

			for (int j = 0; j < num_vms_in_tier[i]; j++) {
				String VM_ID = ID + "_" + i;
				VirtualMachine vm = new VirtualMachine(VM_ID, type);
				this.addVM(vm, i);
			}

		}

		
		// check
		for (int i = 0; i < num_tiers; i++)
		{
			assert(num_vms_in_tier[i] == vms_tiers.get(i).size());
			
		}
	}

	private void connectVMsWithLinks() {		
		// connect Intra tier connections (VMLink)
//		for (int i = 0; i < num_tiers; i++) {
//			List<VirtualMachine> tier_i  = vms_tiers.get(i);
//			if (tier_i.size() > 1)
//			{
//				// sample once and use for all VMs in the tier
//				VirtualMachineLinkType lnkType = sampleLinkType(tier_i.get(0), tier_i.get(1));
//				for (VirtualMachine vm_i : tier_i)
//					for (VirtualMachine vm_j : tier_i)
//					{
//						if (!vm_i.equals(vm_j))	
//						{
//							VirtualMachineLink     vmLink = new VirtualMachineLink(vm_i , vm_j, lnkType);
//							vm_i.addLink(vmLink);
//							vm_j.addLink(vmLink);
//							
//							
//						}
//						
//					}
//			}
//
//		}
		
		
		
		// Inter tier connection
		// connect only from tier 0 -> num_tiers - 1
		for (int i = 0; i < num_tiers - 1; i++) {
			List<VirtualMachine> tier_i  = vms_tiers.get(i);
			List<VirtualMachine> tier_i_plus_1  = vms_tiers.get(i + 1);
			VirtualMachineLinkType lnkType = sampleLinkType(tier_i.get(0), tier_i_plus_1.get(0));
			
			for (VirtualMachine vm_tier_i : tier_i)
				for (VirtualMachine vm_tier_i_plus_1 : tier_i_plus_1) {
			
					VirtualMachineLink     vmLink = new VirtualMachineLink(vm_tier_i , vm_tier_i_plus_1, lnkType);
					vm_tier_i.addLink(vmLink);
					vm_tier_i_plus_1.addLink(vmLink);
				}
			
		}

	}

	/**
	 * Random sampling according to probability
	 */
	private void initVDC() {

		

		
		// sample numVMs in each tier (num_vms_in_tier[])
		allocateNumVMsInTiers();

		// have numVMs -> create and allocate VMS to each tier
		addActualVMsToEachTier();

		// create links between each VMs follow the meshing network
		connectVMsWithLinks();

	}

	public void addVM(VirtualMachine vm, int tier_index) {
		if (vmset.contains(vm)) {
			System.out.println(" VM is already contained , exit now");
			System.exit(1);

		}
		vmset.add(vm);
		vm.setVdc(this);

		vms_tiers.get(tier_index).add(vm);

	}

	public void removeVM(VirtualMachine vm, int tier_index) {
		if (!vmset.contains(vm)) {
			System.out.println(" VM is not exist , exit now");
			System.exit(1);

		}
		vmset.remove(vm);
		vm.setVdc(null);

		vms_tiers.get(tier_index).remove(vm);
	}

}
