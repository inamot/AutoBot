package se.kau.cs.jittac.model.mapping.events;

import se.kau.cs.jittac.model.mapping.ArchitectureMapping;

/**
 * Interface for change events in architecture mappings.
 * @author Sebastian Herold
 *
 */
public interface ArchitectureMappingChangeEvent {

	/**
	 * Returns the mapping that was modified through this event.
	 * @return see above.
	 */
	ArchitectureMapping getMapping();
}
