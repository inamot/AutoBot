package se.kau.cs.jittac.model.im.events;

import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;

public interface BuildEventListener {

	public void onBuildStart(BuildStartEvent event);
	
	public void onBuildEnd(BuildEndEvent event);
	
	public void onBuildEvent(BuildEvent event);
}
