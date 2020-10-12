package se.kau.cs.jittac.model.mapping;

/**
 * An incomplete default implementation for project resources in Jittac.
 * @author Sebastian Herold
 *
 */
public abstract class AbstractJittacProject extends AbstractMappableElement implements IJittacProject {

	
	@Override
	public IJittacProject getProject() {
		return this;
	}

	@Override
	public IJittacResource getParent() {
		return null;
	}

	@Override
	public boolean isDescendantOf(IMappableElement elem) {
		return false;
	}
	
}
