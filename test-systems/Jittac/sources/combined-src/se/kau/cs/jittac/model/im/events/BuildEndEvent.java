package se.kau.cs.jittac.model.im.events;

import se.kau.cs.jittac.model.im.ImplementationModelPartition;

public class BuildEndEvent implements BuildEvent {

	private ImplementationModelPartition part;
	
	public BuildEndEvent(ImplementationModelPartition part) {
		this.part = part;
	}
	
	@Override
	public ImplementationModelPartition getPartition() {
		return part;
	}

}