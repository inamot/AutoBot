package se.kau.cs.jittac.model.mapping.events;

public abstract class AbstractArchitectureMappingChangeListener implements IArchitectureMappingChangeListener {

	@Override
	public void onEvent(ArchitectureMappingChangeEvent event) {
		if (event == null) return;
		if (event instanceof MappingAdditionEvent) {
			onMappingAdded((MappingAdditionEvent) event);
		}
		else if (event instanceof MappingRemovalEvent) {
			onMappingRemoved((MappingRemovalEvent) event);
		}
		else if (event instanceof ManagedProjectAddedEvent) {
			
		}
		else if (event instanceof ManagedProjectRemovedEvent) {
			
		}
		
	}
	
	protected void onMappingAdded(MappingAdditionEvent event) {
	}

	protected void onMappingRemoved(MappingRemovalEvent event) {
	}
	
	protected void onManagedProjectAdded(ManagedProjectAddedEvent event) {
		
	}
	
	protected void onManagedProjectRemoved(ManagedProjectRemovedEvent event) {
		
	}
}
