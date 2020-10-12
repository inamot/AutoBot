package se.kau.cs.jittac.model.am.events;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Connector;

public class ConnectorRemovalEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Connector conn;
	
	public ConnectorRemovalEvent(Connector conn, ArchitectureModel am) {
		this.conn = conn;
		this.am = am;
	}
	
	public Connector getRemovedConnector() {
		return conn;
	}
	
	public ArchitectureModel getModel() {
		return am;
	}
	

}
