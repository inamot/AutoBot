package se.kau.cs.jittac.model.mapping;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.im.IImplementationModel;

/**
 * This interface represents resources of type "Project" which
 * are the top-level resources.
 * @author Sebastian Herold
 *
 */
public interface IJittacProject extends IMappableElement {

	/**
	 * Returns the implementation model that this project is linked to.
	 * @return
	 */
	public IImplementationModel getImplementationModel();
	
	/**
	 * Returns the architecture model that this project is linked to.
	 * @return
	 */
	public ArchitectureModel getArchitectureModel();
}
