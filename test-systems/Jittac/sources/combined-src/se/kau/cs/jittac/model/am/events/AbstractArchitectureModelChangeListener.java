package se.kau.cs.jittac.model.am.events;

public abstract class AbstractArchitectureModelChangeListener implements IArchitectureModelChangeListener {

	@Override
	public void onComponentAdded(ComponentAdditionEvent event) {
	}

	@Override
	public void onComponentRemoved(ComponentRemovalEvent event) {
	}

	@Override
	public void onConnectorStateChange(ConnectorStateChangeEvent event) {
	}
	
	@Override
	public void onConnectorAdded(ConnectorAdditionEvent event) {
	}

	@Override
	public void onConnectorRemoved(ConnectorRemovalEvent event) {
	}


	@Override
	public void onConnectorReferencesAdded(ConnectorReferencesAddedEvent event) {
	}

	@Override
	public void onConnectorReferencesRemoved(ConnectorReferencesRemovedEvent event) {
	}

	@Override
	public void onComponentNameChanged(ComponentNameChangeEvent event) {
		
	}

	@Override
	public void handleEvent(ArchitectureModelChangeEvent event) {
		if (event instanceof ComponentAdditionEvent) {
			this.onComponentAdded((ComponentAdditionEvent) event);
		}
		else if (event instanceof ComponentRemovalEvent) {
			this.onComponentRemoved((ComponentRemovalEvent) event);
		}
		else if (event instanceof ConnectorAdditionEvent) {
			this.onConnectorAdded((ConnectorAdditionEvent) event);
		}
		else if (event instanceof ComponentNameChangeEvent) {
			this.onComponentNameChanged((ComponentNameChangeEvent) event);
		}
		else if (event instanceof ConnectorStateChangeEvent) {
			this.onConnectorStateChange((ConnectorStateChangeEvent) event);
		}
		else if (event instanceof ConnectorRemovalEvent) {
			this.onConnectorRemoved((ConnectorRemovalEvent) event);
		}
		else if (event instanceof ConnectorReferencesAddedEvent) {
			this.onConnectorReferencesAdded((ConnectorReferencesAddedEvent) event);
		}
		else if (event instanceof ConnectorReferencesRemovedEvent) {
			this.onConnectorReferencesRemoved((ConnectorReferencesRemovedEvent) event);
		}
	}

	
}
