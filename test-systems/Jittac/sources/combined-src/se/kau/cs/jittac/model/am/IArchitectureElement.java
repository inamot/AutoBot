package se.kau.cs.jittac.model.am;

import java.util.UUID;

/**
 * Interface representing commonalities of architecture model elements.
 * @author Sebastian Herold
 *
 */
public interface IArchitectureElement {
	
	/**
	 * Returns the model containing the model element or, in case the element is a model, the model itself.
	 * @return see above.
	 */
	public ArchitectureModel getModel();
	
	/**
	 * Returns the identifier of the element
	 * @return see above.
	 */
	public UUID getId();
	
	/**
	 * Returns the human-readable name for the element
	 * @return The element's name as string.
	 */
	public String getName();
	
}
