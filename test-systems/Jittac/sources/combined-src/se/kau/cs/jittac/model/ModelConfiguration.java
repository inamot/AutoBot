package se.kau.cs.jittac.model;

import java.util.Collection;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IJittacProject;

public interface ModelConfiguration {
	
	public ArchitectureModel getArchitectureModel();

	public ArchitectureMapping getArchitectureMapping();
	
	public Collection<IJittacProject> getProjects();

}
