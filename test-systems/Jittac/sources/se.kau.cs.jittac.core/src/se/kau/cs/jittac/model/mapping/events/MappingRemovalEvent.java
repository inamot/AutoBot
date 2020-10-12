package se.kau.cs.jittac.model.mapping.events;

import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IMappableElement;

public class MappingRemovalEvent implements ArchitectureMappingChangeEvent {

	private ArchitectureMapping mapping;
	private Component component, componentAfterRemoval;
	private IMappableElement element;

	public MappingRemovalEvent(Component comp,
								IMappableElement elem,
								Component componentAfterRemoval,
								ArchitectureMapping mapping) {
		this.mapping = mapping;
		this.component = comp;
		this.element = elem;
		this.componentAfterRemoval = componentAfterRemoval;
	}
	
	@Override
	public ArchitectureMapping getMapping() {
		return this.mapping;
	}
	
	public Component getComponent() {
		return component;
	}
	
	public IMappableElement getElement() {
		return element;
	}
	
	public Component getComponentAfterRemoval() {
		return componentAfterRemoval;
	}
}
