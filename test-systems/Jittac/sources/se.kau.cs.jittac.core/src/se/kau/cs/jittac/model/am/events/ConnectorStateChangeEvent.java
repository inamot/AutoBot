package se.kau.cs.jittac.model.am.events;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Connector;

public class ConnectorStateChangeEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Connector conn;
	private Connector.STATE oldState, newState;
	
	public ConnectorStateChangeEvent(Connector conn, Connector.STATE oldState,
			Connector.STATE newState, ArchitectureModel am) {
		this.conn = conn;
		this.oldState = oldState;
		this.newState = newState;
		this.am = am;
	}
		
	public Connector getConnector() {
		return conn;
	}

	public Connector.STATE getOldState() {
		return oldState;
	}

	public Connector.STATE getNewState() {
		return newState;
	}

	@Override
	public ArchitectureModel getModel() {
		return am;
	}

}
