package se.kau.cs.jittac.model.am;

import java.util.UUID;

/**
 * Basic abstract implementation of architecture elements.
 * @author Sebastian Herold
 *
 */
public abstract class AbstractArchitectureElement implements IArchitectureElement {

	private final UUID id;
	
	protected AbstractArchitectureElement() {
		this(null);
	}
	
	protected AbstractArchitectureElement(UUID id) {
		if (id == null) {
			this.id = UUID.randomUUID();
		}
		else {
			this.id = id;
		}
	}

	public final UUID getId() {
		return id;
	}
	
	public String toString() {
		return getName();
	}
}
