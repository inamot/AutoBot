package se.kau.cs.jittac.model.mapping.eclipse;

import java.util.Set;

import org.eclipse.core.resources.IResource;

import se.kau.cs.jittac.model.mapping.AbstractJittacResource;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IMappableElement;

public class EclipseJittacResource extends AbstractJittacResource {

	private IResource resource;
	private EclipseJittacProject project;

	public static EclipseJittacResource create(IResource resource, EclipseJittacProject project) {
		return new EclipseJittacResource(resource, project);
	}
	
	private EclipseJittacResource(IResource resource, EclipseJittacProject project) {
		this.resource = resource;
		this.project = project;
	}
	
	@Override
	public IJittacProject getProject() {
		return project;
	}

	@Override
	public IMappableElement getParent() {
		IResource eParent = resource.getParent();
		if (eParent == null) {
			return null;
		}
		else {
			return project.getResource(eParent);
		}
	}

	@Override
	public Set<IJittacResource> getChildren() {
		return project.getChildrenFor(this.resource);
	}

	public IResource getWrappedResource() {
		return resource;
	}

	@Override
	public String getPersistentHandle() {
		return this.resource.getFullPath().toPortableString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	public String toString() {
		return getWrappedResource().getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EclipseJittacResource other = (EclipseJittacResource) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}

	@Override
	public String getResourceModelName() {
		return EclipseJittacResourceFactory.RESOURCE_MODEL_NAME;
	}
	
	
}
