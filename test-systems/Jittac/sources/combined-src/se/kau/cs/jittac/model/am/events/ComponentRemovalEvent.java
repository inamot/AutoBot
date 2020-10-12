package se.kau.cs.jittac.model.am.events;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;

public class ComponentRemovalEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Component comp;
	
	public ComponentRemovalEvent(Component comp, ArchitectureModel am) {
		this.comp = comp;
		this.am = am;
	}
	
	public Component getRemovedComponent() {
		return comp;
	}
	
	public ArchitectureModel getModel() {
		return am;
	}
	

}
