package se.kau.cs.jittac.model.im;

import java.util.HashSet;
import java.util.Set;

import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.events.XReferenceChangeDeltaEvent;
import se.kau.cs.jittac.model.mapping.IJittacResource;

public class BuildUnit {
	private IJittacResource resource;
	private ImplementationModelPartition partition;
	
	private Set<IXReference<? extends IImplementationModelElement<?>,?>> common;				/// Unchanged references in current compilation unit.
	private Set<IXReference<? extends IImplementationModelElement<?>,?>> added;				/// References added to current compilation unit.
	private Set<IXReference<? extends IImplementationModelElement<?>,?>> current;
	
	private Set<IXReference<? extends IImplementationModelElement<?>,?>> incoming;
	boolean beingProcessed = false;

	
	public BuildUnit(IJittacResource resource, ImplementationModelPartition partition) {
		this.resource = resource;
		this.partition = partition;
		common = new HashSet<>();
		added = new HashSet<>();
		current = new HashSet<>();
		incoming = new HashSet<>();
	}
	
	/**
	 * Adds a reference to the implementation model.
	 * @param xref The reference to be added.
	 */
	public void addXReference(IXReference<? extends IImplementationModelElement<?>,?> xref) {
		if (!beingProcessed) return;
		//ArrayList<IXReference<T>> temp = new ArrayList<>(current);
		if (current.remove(xref)) {
			common.add(xref);
		} else {
			added.add(xref);
		}
	}
	
	public void loadReference(IXReference<? extends IImplementationModelElement<?>,?> xref) {
		if (beingProcessed) return;
		current.add(xref);
		getOppositeBuildUnit(xref).addIncomingReference(xref);
	}
	
	public void start() {
		if (beingProcessed) return;
		beingProcessed = true;
		common.clear();
		added.clear();
	}
	
	public void end() {
		if (!beingProcessed) return;
		Set<IXReference<? extends IImplementationModelElement<?>,?>> removed = new HashSet<>();
		removed.addAll(current);
		current.clear();
		current.addAll(common);
		current.addAll(added);
		for (IXReference<?,?> ref : removed) {
			getOppositeBuildUnit(ref).removeIncomingReference(ref);
		}
		for (IXReference<?,?> ref : added) {
			getOppositeBuildUnit(ref).addIncomingReference(ref);
		}
		
		beingProcessed = false;
		//fire event
		this.partition.
			getImplementationModel().
				fireChangeEvent(XReferenceChangeDeltaEvent.create(added, removed, common));
	}
	
	public void clear() {
		added.clear();
		common.clear();
		current.clear();
	}
	
	public Set<IXReference<?,?>> getReferences() {
		//TODO: better throw exception if unit is being processed
		if (beingProcessed) return new HashSet<IXReference<? extends IImplementationModelElement<?>,?>>();
		return new HashSet<IXReference<? extends IImplementationModelElement<?>,?>>(current);
	}
	
	public Set<IXReference<?,?>> getIncomingReferences() {
		return new HashSet<IXReference<?,?>>(incoming);
	}
	
	private boolean addIncomingReference(IXReference<? extends IImplementationModelElement<?>,?> ref) {
		if (!incoming.contains(ref)) {
			incoming.add(ref);
			return true;
		}
		return false;
	}
	
	private boolean removeIncomingReference(IXReference<? extends IImplementationModelElement<?>,?> ref) {
		return incoming.remove(ref);
	}
	
	private BuildUnit getOppositeBuildUnit(IXReference<? extends IImplementationModelElement<?>,?> ref) {
		IImplementationModelElement<?> trg = ref.getTarget();
		return trg.getPartition().getUnit(trg.getResource());
	}
	
	public IJittacResource getResource() {
		return resource;
	}
}
