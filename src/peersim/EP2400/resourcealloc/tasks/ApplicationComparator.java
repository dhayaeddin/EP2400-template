package peersim.EP2400.resourcealloc.tasks;
import java.util.Comparator;

import peersim.EP2400.resourcealloc.base.*;


public class ApplicationComparator implements Comparator<VirtualMachine>{

	@Override
	public int compare(VirtualMachine arg0, VirtualMachine arg1) {
		
		if (arg0.getCPUDemand() > arg1.getCPUDemand()) return 1;
		else if (arg0.getCPUDemand() == arg1.getCPUDemand()) 
			{ 
			   if (arg0.getID().compareTo(arg1.getID()) == 1) return 1;
			   else if (arg0.getID().compareTo(arg1.getID()) == -1) return -1;
			   else
			   { 
				   // should never come here
				   assert false;
				   System.out.println(" Application comparation error, two applications having same ID");
				   System.exit(1);
				   return 0;
			   }
			}
		else return -1;
	}

}
