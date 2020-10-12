package se.kau.cs.jittac.model.am.events;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;

public class ComponentNameChangeEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Component comp;
	private String oldName, newName;
	
	public ComponentNameChangeEvent(Component comp, ArchitectureModel am, String oldname, String newName) {
		this.comp = comp;
		this.am = am;
		this.oldName = oldname;
		this.newName = newName;
	}
	
	@Override
	public ArchitectureModel getModel() {
		return am;
	}
	
	public Component getComponent() {
		return comp;
	}
	
	public String getOldName() {
		return oldName;
	}

	public String getNewName() {
		return newName;
	}
}
