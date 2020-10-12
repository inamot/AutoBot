package se.kau.cs.jittac.model.am.events;

import java.util.HashSet;
import java.util.Set;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.im.IXReference;

public class ConnectorReferencesAddedEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Connector conn;
	private Set<IXReference<?,?>> addedRefs;
	
	public ConnectorReferencesAddedEvent(Connector conn, Set<IXReference<?,?>> addedRefs, ArchitectureModel am) {
		this.conn = conn;
		this.am = am;
		this.addedRefs = new HashSet<IXReference<?,?>>(addedRefs);
	}
	
	public Connector getModifiedConnector() {
		return conn;
	}
	
	public ArchitectureModel getModel() {
		return am;
	}
	
	public Set<IXReference<?,?>> getAddedReferences() {
		return new HashSet<IXReference<?,?>>(this.addedRefs);
	}
}
