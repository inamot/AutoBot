package se.kau.cs.jittac.model.im;

import se.kau.cs.jittac.model.mapping.IJittacResource;

/**
 * Interface representing elements in implementation models
 * that are connected via references.
 * 
 * @author Sebastian Herold
 *
 */
public interface IImplementationModelElement<T> {

	/**
	 * Returns the implementation model that the element belongs to.
	 * @return see above.
	 */
	public ImplementationModelPartition getPartition();
	/**
	 * Returns the resource that the element is contained in.
	 * @return see above.
	 */
	public IJittacResource getResource();
	
	public T getElement();
}
