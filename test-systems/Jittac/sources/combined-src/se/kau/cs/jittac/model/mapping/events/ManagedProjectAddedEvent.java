package se.kau.cs.jittac.model.mapping.events;

import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IJittacProject;

public class ManagedProjectAddedEvent implements ArchitectureMappingChangeEvent {

	private IJittacProject project;
	private ArchitectureMapping mapping;
	
	public ManagedProjectAddedEvent(IJittacProject project, ArchitectureMapping mapping) {
		this.project = project;
		this.mapping = mapping;
	}
	
	@Override
	public ArchitectureMapping getMapping() {
		return mapping;
	}
	
	public IJittacProject getProject() {
		return project;
	}

}
