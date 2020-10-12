package se.kau.cs.jittac.model.mapping.eclipse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.ImplementationModel;
import se.kau.cs.jittac.model.mapping.AbstractJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IMappableElement;

public class EclipseJittacProject extends AbstractJittacProject {

	private final IProject project;
	private final IImplementationModel im;
	private ArchitectureModel am;
	
	private Map<IResource, EclipseJittacResource> resourceMapping;
	
	//TODO: move instance creation to factory
	private static Map<IProject, EclipseJittacProject> createdInstances = new HashedMap<>();
	
	public static EclipseJittacProject get(IProject project) {
		
		EclipseJittacProject result = createdInstances.get(project);
		if (result == null) {
			result = new EclipseJittacProject(project);
			if (result != null) {
				createdInstances.put(project, result);
			}
		}
		return result;
	}

	private EclipseJittacProject(IProject project) {
		this.project = project;
		im = new ImplementationModel(this);
		resourceMapping = new HashedMap<IResource, EclipseJittacResource>();
	}
	
	@Override
	public IImplementationModel getImplementationModel() {
		return im;
	}

	public IProject getWrappedProject() {
		return project;
	}
	
	@Override
	public Set<IJittacResource> getChildren() {
		return getChildrenFor(this.project) ;
	}

	public EclipseJittacResource getResource(IResource resource) {
		if (resourceMapping.containsKey(resource)) {
			return resourceMapping.get(resource);
		}
		else {
			EclipseJittacResource newResource = EclipseJittacResource.create(resource, this);
			if (newResource != null) {
				resourceMapping.put(resource, newResource);
				return newResource;
			}
			else {
				return null;
			}
		}
	}

	public Set<IJittacResource> getChildrenFor(IResource resource) {
		Set<IJittacResource> result = new HashSet<>();
		if (resource != null && resource instanceof IContainer) {
			IContainer wrappedContainer = (IContainer) resource;
			EclipseJittacResource child;
			try {
				for (IResource wrappedChild : wrappedContainer.members()) {
					child = this.getResource(wrappedChild);
					if (child != null) {
						result.add(child);
					}
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}
		}
		return result;
	}

	@Override
	public String getPersistentHandle() {
		return this.project.getFullPath().toPortableString();
	}

	@Override
	public ArchitectureModel getArchitectureModel() {
		return am;
	}

	public String toString() {
		return getWrappedProject().getName();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EclipseJittacProject other = (EclipseJittacProject) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

	@Override
	public String getResourceModelName() {
		return EclipseJittacResourceFactory.RESOURCE_MODEL_NAME;
	}
	
	
}
