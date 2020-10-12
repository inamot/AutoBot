package se.kau.cs.jittac.model.im;

public interface ImplementationModelFactory
<T extends IImplementationModelElement<E>,E,
 U extends IXReference<T, F> ,F>	 {

	public T createElement(E element, ImplementationModelPartition partition);
	
	public U createReference(
		T src,
		T trg,
		IXReferenceType type,
		F internalRef);
	
	public IXReference<T,F> createExternalReference(
			T src,
			IImplementationModelElement<?> trg,
			IXReferenceType type,
			F internalRef);
	
	public ImplementationModelPartitionType getPartitionType();
	
	public String serializeInternalReference(IXReference<?,?> reference);
	
	public String serializeExternalReference(IXReference<?,?> reference);
	
	public String serializeLocalElement(T element);
	
	public T deserializeLocalElement(String elementAsString, ImplementationModelPartition partition);
	
	public U deserializeInternalReference(String referenceAsString, 
			ImplementationModelPartition partition);
	
	public IXReference<T,?> deserializeExternalReference(String referenceAsString,
			ImplementationModelPartition partition);
}
