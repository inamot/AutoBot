package se.kau.cs.jittac.model.im;

import se.kau.cs.jittac.model.mapping.IJittacResource;

/**
 * This class encapsulates references which are the primary 
 * elements in implementation models. They represent different
 * ways of how elements in the implementation of a system might 
 * depend on other elements (e.g., calls to methods, import 
 * statements, inheritance,...). A reference is always directed.
 * 
 * @author Sebastian Herold
 */
public interface IXReference<T extends IImplementationModelElement<?>, U> {
	/**
	 * Returns the reference's model partition that the reference belongs to.
	 * In case of references connecting elements in different
	 * implementation models, this is, by definition, the model
	 * that the reference's source element is belonging to.
	 * @return see above.
	 */
	public ImplementationModelPartition getPartition();
	/**
	 * Returns the type of the reference.
	 * @return see above.
	 */
	public IXReferenceType getType();
	/**
	 * Returns the source element of the violation
	 * @return See above.
	 */
	public T getSource();
	/**
	 * Returns the target element of the violation
	 * @return see above.
	 */
	public IImplementationModelElement<?> getTarget();
	/**
	 * Returns the "physical" resource that the reference belongs
	 * to. By definition, this is the same resource that the source
	 * of the dependency belongs to.
	 * @return see above.
	 */
	public IJittacResource getResource();
	
	/**
	 * Returns some internal representation of the reference, e.g. the AST node
	 * representing the call/access/etc. in source code.
	 * @return
	 */
	public U getInternalReference();
}
