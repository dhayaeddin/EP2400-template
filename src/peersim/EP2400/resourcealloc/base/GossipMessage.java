package peersim.EP2400.resourcealloc.base;

import peersim.core.Node;

public class GossipMessage {

	
	private PhysicalMachine machine;
	public PhysicalMachine getMachine() {
		return machine;
	}

	public void setMachine(PhysicalMachine machine) {
		this.machine = machine;
	}

	public Node getSender() {
		return sender;
	}

	public void setSender(Node sender) {
		this.sender = sender;
	}

	public boolean sendBack() {
		return sendBack;
	}

	public void setActiveThread(boolean activeThread) {
		this.sendBack = activeThread;
	}

	private Node sender;
	private boolean sendBack;
	
	public GossipMessage( Node sender, PhysicalMachine machine ,  boolean sendBack
			)
	{
		this.machine = machine;
		this.sender = sender;
		this.sendBack = sendBack;
		
		
	}
}
