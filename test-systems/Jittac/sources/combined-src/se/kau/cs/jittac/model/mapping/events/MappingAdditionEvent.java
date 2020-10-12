package se.kau.cs.jittac.model.mapping.events;

import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IMappableElement;

/**
 * Event to represent the addition of a mapping to an architecture
 * mapping.
 * @author sebahero
 *
 */
public class MappingAdditionEvent implements ArchitectureMappingChangeEvent {

	private ArchitectureMapping mapping;
	private Component component, prevComp;
	private IMappableElement element;

	/**
	 * Create a mapping addition event.
	 * @param comp The component for which the new mapping was created.
	 * @param elem The resource for which the new mapping was created.
	 * @param prevComp The component that the resource was previously mapped to.
	 * @param mapping The architecture mapping that was changed by the event.
	 */
	public MappingAdditionEvent(Component comp, IMappableElement elem, Component prevComp, ArchitectureMapping mapping) {
		this.mapping = mapping;
		this.component = comp;
		this.element = elem;
		this.prevComp = prevComp;
	}
	
	@Override
	public ArchitectureMapping getMapping() {
		return this.mapping;
	}
	
	/**
	 * Returns the component that was newly mapped.
	 * @return see above.
	 */
	public Component getComponent() {
		return component;
	}
	
	/**
	 * Returns the component to which the resource was
	 * previously mapped.
	 * @return see above.
	 */
	public Component getPreviousComponent() {
		return prevComp;
	}
	
	/**
	 * Returns the resource that is newly mapped.
	 * @return see above.
	 */
	public IMappableElement getElement() {
		return element;
	}
}
