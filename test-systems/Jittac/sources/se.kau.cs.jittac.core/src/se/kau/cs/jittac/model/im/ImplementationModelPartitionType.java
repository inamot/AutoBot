package se.kau.cs.jittac.model.im;

/**
 * Interface to be implemented for different types of
 * builders, e.g. Java builder based on JDT.
 * @author Sebastian Herold
 *
 */
public interface ImplementationModelPartitionType {
	
	public String getName();
	
	public Class<?> getElementType();
	
	public Class<?> getReferenceType();
	
	//public ImplementationModelFactory<T,E,U,F> getFactory();
}
