package se.kau.cs.jittac.model.am.events;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Connector;

public class ConnectorAdditionEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Connector conn;
	
	public ConnectorAdditionEvent(Connector conn, ArchitectureModel am) {
		this.conn = conn;
		this.am = am;
	}
	
	public Connector getAddedConnector() {
		return conn;
	}
	
	public ArchitectureModel getModel() {
		return am;
	}
	

}
