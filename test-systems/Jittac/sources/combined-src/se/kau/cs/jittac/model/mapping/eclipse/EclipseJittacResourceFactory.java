package se.kau.cs.jittac.model.mapping.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IJittacResourceFactory;

public class EclipseJittacResourceFactory implements IJittacResourceFactory {

	public static final String RESOURCE_MODEL_NAME = "eclipse-resource-model";
	
	public static EclipseJittacResourceFactory INSTANCE =
			new EclipseJittacResourceFactory();
	
	private EclipseJittacResourceFactory() {
	}
	
	@Override
	public IJittacProject createJittacProjectFromPersistentHandle(String handle) {
		
		IJittacProject result = null;
		IResource resource = getResourceFromString(handle);
		if (resource != null) {
			if (resource instanceof IProject) {
				result = EclipseJittacProject.get((IProject) resource);
			}
		}
		return result;
	}

	@Override
	public IJittacResource createJittacResourceFromPersistentHandle(
			String handle, IJittacProject project) {
		
		IResource resource = getResourceFromString(handle);
		if (project instanceof EclipseJittacProject) {
			IProject eclProject = ((EclipseJittacProject) project).getWrappedProject();
			if (resource != null && eclProject != null) {
				if (eclProject.equals(resource.getProject())) {
					//return EclipseJittacResource.create(resource, (EclipseJittacProject) project);
					return ((EclipseJittacProject) project).getResource(resource);
				}
			}
		}
		return null;
	}

	@Override
	public String getResourceModelName() {
		return RESOURCE_MODEL_NAME;
	}

	private IResource getResourceFromString(String protableString) {
		IPath path =  Path.fromPortableString(protableString);
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		return resource;
	}
	
}
