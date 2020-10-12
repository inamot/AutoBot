package se.kau.cs.jittac.model.im.events;

import java.util.HashSet;
import java.util.Set;

import se.kau.cs.jittac.model.im.IXReference;

/**
 * This class encapsulates information about modifications in
 * an implementation model and is passed on to the corresponding listeners.
 * The information is stored in terms of added and removed references, as well
 * as unchanged elements to be easily able to detect references that stayed
 * the same in a certain scope of the implementation model (such as a build increment).
 * @author sebahero
 *
 */
public class XReferenceChangeDeltaEvent {

	private Set<IXReference<?,?>> added;
	private Set<IXReference<?,?>> removed;	
	private Set<IXReference<?,?>> unchanged;

	/**
	 * Creates an event object for a modification corresponding to the given
	 * sets of references
	 * @param added References added through this modification. May not be null.
	 * @param removed References removed through this modification. May not be null.
	 * @param unchanged References that should be declared as unchanged through this modification. May not be null.
	 * @return A new event object, never null.
	 */
	public static XReferenceChangeDeltaEvent create(Set<? extends IXReference<?,?>> added, 
			Set<? extends IXReference<?,?>> removed, Set<? extends IXReference<?,?>> unchanged) {
		return new XReferenceChangeDeltaEvent(added, removed, unchanged);
	}
	
	private XReferenceChangeDeltaEvent(Set<? extends IXReference<?,?>> added, 
			Set<? extends IXReference<?,?>> removed, Set<? extends IXReference<?,?>> unchanged) {
		this.added = new HashSet<IXReference<?,?>>(added);
		this.removed = new HashSet<IXReference<?,?>>(removed);
		this.unchanged = new HashSet<IXReference<?,?>>(unchanged);
	}
	
	/**
	 * Returns the added references as set. Never null.
	 * @return see above.
	 */
	public Set<IXReference<?,?>> addedReferences() {
		return new HashSet<IXReference<?,?>>(added);
	}
	
	/**
	 * Returns the set of removed references. Never null.
	 * @return see above.
	 */
	public Set<IXReference<?,?>> removedReferences() {
		return new HashSet<IXReference<?,?>>(removed);
	}
	
	/**
	 * Returns the set of references stored as unchanged. Never null.
	 * @return see above.
	 */
	public Set<IXReference<?,?>> unchangedReferences() {
		return new HashSet<IXReference<?,?>>(unchanged);
	}
	
}
