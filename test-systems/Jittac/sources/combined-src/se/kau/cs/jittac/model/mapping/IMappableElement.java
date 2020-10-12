package se.kau.cs.jittac.model.mapping;

import java.util.Set;

/**
 * This is the interface for both available types of resources - projects
 * and "normal" resources.
 * 
 * @author Sebastian Herold
 *
 */
public interface IMappableElement {

	/**
	 * Returns the project that a resource belongs to.
	 * @return The project.
	 */
	public IJittacProject getProject();
	
	/**
	 * Returns the parent resource of a resource.
	 * @return see above.
	 */
	public IMappableElement getParent();
	
	/**
	 * Returns the child resources of a resource
	 * @return see above.
	 */
	public Set<IJittacResource> getChildren();
	
	/**
	 * Checks if the current resource is direct or indirect child
	 * of a given resource 
	 * @param elem The resource for which it is checked whether the
	 * current element is a descendant of.
	 * @return True if the current resource is a descendant of the given
	 * resource. False otherwise.
	 */
	public boolean isDescendantOf(IMappableElement elem);
	
	/**
	 * Provides a string representation of an identifier for this resource.
	 * This must be persistent, i.e. it stays the same over different
	 * executions of the tool.
	 * @return A persistent identifier as string.
	 */
	public String getPersistentHandle();
	
	public String getResourceModelName();
}
