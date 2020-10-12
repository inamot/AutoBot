package se.kau.cs.jittac.model.mapping;

public interface IJittacResourceFactory {

	/**
	 * Creates a project resource from the given persistent handle.
	 * @param handle The persistent handle.
	 * @return The corresponding resource if handle can be interpreted.
	 * Null otherwise.
	 */
	public IJittacProject createJittacProjectFromPersistentHandle(String handle);
	
	/**
	 * Creates a non-project resource from the given persistent handle.
	 * @param handle The persistent handle.
	 * @return The corresponding resource if handle can be interpreted.
	 * Null otherwise.
	 */
	public IJittacResource createJittacResourceFromPersistentHandle(String handle, IJittacProject project);
	
	/**
	 * Returns the name of the resource model for which this object is a factory.
	 * @return
	 */
	public String getResourceModelName();
}
