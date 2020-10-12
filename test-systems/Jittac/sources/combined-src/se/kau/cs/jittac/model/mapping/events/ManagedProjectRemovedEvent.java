package se.kau.cs.jittac.model.mapping.events;

import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IJittacProject;

public class ManagedProjectRemovedEvent implements ArchitectureMappingChangeEvent {

	private IJittacProject project;
	private ArchitectureMapping mapping;
	
	public ManagedProjectRemovedEvent(IJittacProject project, ArchitectureMapping mapping) {
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
