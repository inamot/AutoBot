package se.kau.cs.jittac.model.am.events;

/**
 * This interface defines handlers for dealing with events raised by changes in architecture models,
 * such as adding or removing components.
 * 
 * Implementations should extend AbstractArchitectureModelChangeListener and override the corresponding
 * handlers (onXXX). They are hooked into handleEvent(...) which will be invoked automatically by any
 * architecture model that the listener is registered, in case an event occurs.   
 * 
 * @author Sebastian Herold
 *
 */
public interface IArchitectureModelChangeListener {
	
	public void onComponentAdded(ComponentAdditionEvent event);
	public void onComponentRemoved(ComponentRemovalEvent event);
	public void onConnectorAdded(ConnectorAdditionEvent event);
	public void onConnectorStateChange(ConnectorStateChangeEvent event);
	public void onConnectorRemoved(ConnectorRemovalEvent event);
	public void onConnectorReferencesAdded(ConnectorReferencesAddedEvent event);
	public void onConnectorReferencesRemoved(ConnectorReferencesRemovedEvent event);
	public void onComponentNameChanged(ComponentNameChangeEvent event);
	
	public void handleEvent(ArchitectureModelChangeEvent event);
}
