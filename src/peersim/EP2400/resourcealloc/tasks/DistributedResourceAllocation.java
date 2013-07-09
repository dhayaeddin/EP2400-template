package peersim.EP2400.resourcealloc.tasks;

import java.util.HashSet;
import java.util.Set;

import peersim.EP2400.resourcealloc.base.GossipMessage;
import peersim.EP2400.resourcealloc.base.ResourcesTriple;
import peersim.EP2400.resourcealloc.base.VirtualMachine;
import peersim.EP2400.resourcealloc.base.VirtualMachinesList;
import peersim.EP2400.resourcealloc.base.PhysicalMachine;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.*;
import peersim.config.*;
import peersim.core.*;

public class DistributedResourceAllocation extends PhysicalMachine {

	private final double MAGIC_NUMBER = 1e-9;

	public DistributedResourceAllocation(String prefix) {
		super(prefix);
	}

	public boolean containsVMWithID(Set<VirtualMachine> set, String ID)
	{
		for (VirtualMachine vm : set)
		{
			if (vm.getID().equals(ID))
				return true;
			
		}
		return false;
		
	}
	
	public void updateState_achieveGoal(PhysicalMachine node_i,
			PhysicalMachine node_j) {
		
		
		do {
			
			boolean foundMove = false;
			VirtualMachine decidedVM = null;

			double max_val = -1;

			// inspect all possible VMs in node_i
			for (VirtualMachine m : node_i.vmsList()) {
				if (ResourceAllocationLogic.isVMMovable(node_i, node_j, m) ) {
					double val = ResourceAllocationLogic
							.valConsiderToAchieveGoal(node_i, node_j, m);
					
					
					double f     = ResourceAllocationLogic.f(node_i, node_j);
					double f_m   = ResourceAllocationLogic.f_m(node_i, node_j, m);
					
					
					if (val > max_val && f > f_m) {
						foundMove = true;
						max_val = val;
						decidedVM = m;
					}

				}

			}
			
			
			// inspect all possible VMs in node j
			for (VirtualMachine m : node_j.vmsList()) {
				if (ResourceAllocationLogic.isVMMovable(node_j, node_i, m) ) {
					double val = ResourceAllocationLogic
							.valConsiderToAchieveGoal(node_j, node_i, m);
					
					
					double f     = ResourceAllocationLogic.f(node_j, node_i);
					double f_m   = ResourceAllocationLogic.f_m(node_j, node_i, m);
				
					if (val > max_val && f > f_m) {
						foundMove = true;
						max_val = val;
						decidedVM = m;
					}

				}
			

			}
			
			if (!foundMove)
				break;
			assert (decidedVM != null);
			
			if (decidedVM.getPhysicalMachine().equals(node_i))
			{
				// execute the movement from node_i
				node_i.deallocateVM(decidedVM);
				node_j.allocateVM(decidedVM);
			}
			else
			{
				// execute the movement from node_j
				node_i.allocateVM(decidedVM);
				node_j.deallocateVM(decidedVM);
			}

		} while (true);

	}

	public void updateState_fixOverload(PhysicalMachine node_i,
			PhysicalMachine node_j) {

		// compute delta
		ResourcesTriple delta_i = ResourceAllocationLogic.delta(node_i);
		ResourcesTriple delta_j = ResourceAllocationLogic.delta(node_j);

		// adjust for overloading situations
		if ((delta_i.plus(delta_j)).twoNorm() > 0
				) {

			 assert (delta_i.twoNorm() > 0);
			 
//			 double overload=max(delta_i.twoNorm(),delta_j.twoNorm());
//			 Module the_m=null;
//			 do {
//				 double max_d_overload=0;
//				 for (m in VMs in i) {
//					 new_overload=max(resourceAllocationLogic.delta_m(node_i, node_j, m)[0].twoNorm(), resourceAllocationLogic.delta_m(node_j, node_i, -m)[0].twoNorm()); 
//					 if new_overload<overloead {
//						 if (overload-new_overload> max_d_overload) {
//							 max_d_overload=overload-new_overload;
//							 the_m=m
//						 }
//					 }
//				 }
//				 for (m in VMs in j) {
//					 new_overload=max(resourceAllocationLogic.delta_m(node_i, node_j, m)[0].twoNorm(), resourceAllocationLogic.delta_m(node_j, node_i, -m)[0].twoNorm()); 
//					 if new_overload<overloead {
//						 if (overload-new_overload> max_d_overload) {
//							 max_d_overload=overload-new_overload;
//							 the_m=m
//						 }
//					 }
//				 }
//			 }
			// find a VM repeatly
			do {
				double max_val = Double.NEGATIVE_INFINITY;

				boolean foundMove = false;
				VirtualMachine decidedVM = null;
				for (VirtualMachine m : node_i.vmsList()) {
					if (ResourceAllocationLogic.isVMMovable(node_i, node_j, m)) {
						double val = ResourceAllocationLogic
								.valueConsideredForFixOverload(node_i, node_j,
										m);
						
						double delta_i_two_norm   = ResourceAllocationLogic.delta(node_i).twoNorm();
						double delta_i_m_two_norm = ResourceAllocationLogic.delta_m(node_i, node_j, m)[0].twoNorm();
						
						if (val > max_val && delta_i_two_norm > delta_i_m_two_norm) {
							foundMove = true;
							max_val = val;
							decidedVM = m;
						}

					}

				}
				if (!foundMove)
					break;

				assert (decidedVM != null);
				// execute the movement
				node_i.deallocateVM(decidedVM);
				node_j.allocateVM(decidedVM);

			} while (ResourceAllocationLogic.delta(node_i).twoNorm() < MAGIC_NUMBER); // similar
																						// to
																						// =
																						// 0.0

		}

	}

	public void updateState(PhysicalMachine peer_one, PhysicalMachine peer_two) {
		
		double delta_one_norm = ResourceAllocationLogic.delta(peer_one).twoNorm();
		double delta_two_norm = ResourceAllocationLogic.delta(peer_two).twoNorm();
		
		PhysicalMachine node_i = delta_one_norm > delta_two_norm? peer_one : peer_two;
		PhysicalMachine node_j = delta_one_norm > delta_two_norm? peer_two : peer_one;

		updateState_fixOverload(node_i, node_j);
		updateState_achieveGoal(node_i, node_j);
	}

	public Object clone() {
		DistributedResourceAllocation proto = new DistributedResourceAllocation(
				this.prefix);
		return proto;
	}

	/**
	 * event-based simulation codes This will be executed twice , one for the
	 * passive thread at the otherside, Then, again activeThread at the sender
	 */
//	@Override
//	public void processEvent(Node node, int pid, Object event) {
//
//		GossipMessage received_msg = (GossipMessage) event;
//		if (received_msg.sendBack()) {
//
//			// GossipMessage msg = new GossipMessage(node, n_prime, true);
//
//			// send the message back
//			GossipMessage msg = new GossipMessage(node, this, false);
//
//			// send the message back
//			Transport trans = (Transport) node.getProtocol(FastConfig
//					.getTransport(pid));
//			trans.send(node, received_msg.getSender(), msg, pid);
//
//		}
//
////		this.updateState(received_msg);
//
//	}

	// Cycle-based simulation code
	@Override
	public void nextCycle(Node node, int protocolID) {


//		int linkableID = FastConfig.getLinkable(protocolID);
//		Linkable linkable = (Linkable) node.getProtocol(linkableID);
//		int degree = linkable.degree();
//		int nbIndex = CommonState.r.nextInt(degree);
//		Node peer = linkable.getNeighbor(nbIndex);
		
		Node peer = null;
		do{
			
		int randomIndex = CommonState.r.nextInt(Network.size());
	    peer = Network.get(randomIndex);
		
		} while (peer.equals(node));
		
		PhysicalMachine peer_m = (PhysicalMachine) peer.getProtocol(protocolID);
		
		if (!Configuration.getBoolean("debug_mode"))
		{
			this.updateLocalDemand();
			peer_m.updateLocalDemand();
		}
		
		this.localResourcesAllocation();
		peer_m.localResourcesAllocation();
		
		updateState(this, peer_m );
		
		//System.out.println("Executing at Time " + CommonState.getTime() + " for node " + node.getID());
		

	}

}
