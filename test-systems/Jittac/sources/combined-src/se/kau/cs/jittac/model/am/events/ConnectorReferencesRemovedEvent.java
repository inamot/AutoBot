package se.kau.cs.jittac.model.am.events;

import java.util.HashSet;
import java.util.Set;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.im.IXReference;

public class ConnectorReferencesRemovedEvent implements ArchitectureModelChangeEvent {

	private ArchitectureModel am;
	private Connector conn;
	private Set<IXReference<?,?>> removedRefs;
	
	public ConnectorReferencesRemovedEvent(Connector conn, Set<IXReference<?,?>> removedRefs, ArchitectureModel am) {
		this.conn = conn;
		this.am = am;
		this.removedRefs = new HashSet<IXReference<?,?>>(removedRefs);
	}
	
	public Connector getModifiedConnector() {
		return conn;
	}
	
	public ArchitectureModel getModel() {
		return am;
	}
	
	public Set<IXReference<?,?>> getRemovedReferences() {
		return new HashSet<IXReference<?,?>>(removedRefs);
	}

}
