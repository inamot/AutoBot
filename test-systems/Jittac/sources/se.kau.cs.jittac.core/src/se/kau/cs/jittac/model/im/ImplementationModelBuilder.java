package se.kau.cs.jittac.model.im;

/**
 * Interface for implementation model builders - classes populating
 * implementation models, e.g. by parsing source code.
 * 
 * @author Sebastian Herold
 *
 */

public interface ImplementationModelBuilder {
	
	/**
	 * Returns the type of a builder.
	 * @return see above.
	 */
	public ImplementationModelPartitionType getType();
	
	public ImplementationModelPartition getPartition();
	
		
	//public void start();
	
	//public boolean isRunning();
}
