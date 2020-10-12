package se.kau.cs.jittac.model.am;

import java.util.HashSet;
import java.util.Set;

import se.kau.cs.jittac.model.im.IXReference;

/**
 * This class represents connectors in architecture models.
 * @author Sebastian Herold
 *
 */
public class Connector extends AbstractArchitectureElement {

	private ArchitectureModel am;
	private final Component src;
	private final Component trg;
	private boolean envisaged;
	private Set<IXReference<?,?>> contributingRefs;

	/**
	 * The state of a connector represents the different types of
	 * connectors in reflexion modelling plus additional states for
	 * reflexive connectors and inconsistent situations (e.g., not
	 * envisaged but convergent).
	 * @author Sebastian Herold
	 *
	 */
	public static enum STATE {
		CONVERGENCE,
		DIVERGENCE,
		ABSENCE,
		INCONSISTENT,
		REFLEXIVE
	}
	
	/**
	 * Creates a connector between two components if none exists between them.
	 * Both components must be in the same architecture model.
	 * @param src Component being the source of the connector. May not be null.
	 * @param trg Component being the target of the connector. May not be null.
	 * @return A new Connector object. Null if one or both components are null,
	 * not in the same model, or if a connector already exists between them.
	 */
	public static Connector createConnector(Component src, Component trg) {
		if (src == null || trg == null /*|| src.equals(trg)*/ || src.getModel() != trg.getModel()) return null;
		if (src.getModel().containsConnectorBetween(src, trg)) return null;
		
		return new Connector(src.getModel(), src, trg);
	}
	
	private Connector(ArchitectureModel am, Component src, Component trg) {
		super();
		this.am = am;
		this.src = src;
		this.trg = trg;
		this.envisaged = true;
		contributingRefs = new HashSet<IXReference<?,?>>();
	}
	
	public ArchitectureModel getModel() {
		return am;
	}
	
	/**
	 * Returns the name of the connector in the form
	 * 
	 * Name of source component -> name of target component
	 */
	public String getName() {
		return src.getName() + " -> " + trg.getName();
	}
	
	/**
	 * Returns the component being the source of the connector.
	 * @return
	 */
	public Component getSrc() {
		return src;
	}

	/**
	 * Returns the component being the target of the connector.
	 * @return
	 */
	public Component getTrg() {
		return trg;
	}

	/**
	 * Sets a boolean value indicating whether the connector is
	 * envisaged, i.e. whether it represents allowed source code
	 * dependencies.
	 * @param value true for "is envisaged", false for "not envisaged".
	 */
	public void setEnvisaged(boolean value) {
		envisaged = value;
	}
	
	/**
	 * Returns whether the connector is envisaged or not.
	 * @return True if the connector is envisaged. Always true
	 * for reflexive connectors. False otherwise.
	 */
	public boolean isEnvisaged() {
		if (isReflexive()) return true;
		return envisaged;
	}
	
	/**
	 * Returns whether the connector is reflexive,
	 * i.e. whether the source component equals the target 
	 * component.
	 * @return see above.
	 */
	public boolean isReflexive() {
		return getSrc().equals(getTrg());
	}
	
	/**
	 * Returns a copy of the set of references contributing
	 * to the connector.
	 * @return see above.
	 */
	public Set<IXReference<?,?>> getContributingReferences() {
		return new HashSet<IXReference<?,?>>(this.contributingRefs);
	}
	
	/**
	 * Adds a reference to the connector
	 * @param ref The reference to be added.
	 * @return True if the set of contributing references was
	 * changed by this addition, the passed reference was
	 * not contained before the call. False otherwise.
	 */
	public boolean addReference(IXReference<?,?> ref) {
		return contributingRefs.add(ref);
	}
	
	/**
	 * Adds a set of references to the connector.
	 * @param refs The set of references to be added.
	 * @return True if the set of contributing references was
	 * changed by this addition, the passed reference was
	 * not contained before the call. False otherwise.
	 */
	public boolean addReferences(Set<IXReference<?,?>> refs) {
		return contributingRefs.addAll(refs);
	}	
	
	/**
	 * Checks whether a reference contributes to the connector.
	 * @param ref The reference to be checked.
	 * @return True if the passed reference contributes, false otherwise.
	 */
	public boolean isContributedBy(IXReference<?,?> ref) {
		return contributingRefs.contains(ref);
	}
	
	/**
	 * Removes a reference from the set of contributing references
	 * @param ref The reference to be removed.
	 * @return True if the set of contributing references was changed.
	 * False otherwise.
	 */
	public boolean removeReference(IXReference<?,?> ref) {
		return contributingRefs.remove(ref);
	}

	/**
	 * Removes references from the set of contributing references
	 * @param refs The references to be removed.
	 * @return True if the set of contributing references was changed.
	 * False otherwise.
	 */
	public boolean removeReferences(Set<IXReference<?,?>> refs) {
		return contributingRefs.removeAll(refs);
	}
	
	/**
	 * Returns the state of the connector.
	 * The state of a reflexive connector is always STATE.REFLEXIVE.
	 * The state of an envisaged connector is STATE.CONVERGENCE unless
	 * there are no contributing references, in which case its state is
	 * STATE.ABSENCE.
	 * The state of a non-envisaged connector is STATE.DIVERGENCE unless
	 * there are no contributing references, in which case its state is
	 * STATE.INCONSISTENT (something is broken then).
	 * 
	 * @return the connector's state according to the description above.
	 */
	public Connector.STATE getState() {
		if (isReflexive()) return STATE.REFLEXIVE;
		if (isEnvisaged()) {
			return contributingRefs.size() > 0 ? STATE.CONVERGENCE : STATE.ABSENCE;
		}
		else {
			return contributingRefs.size() > 0 ? STATE.DIVERGENCE : STATE.INCONSISTENT;
		}
	}
	
	public void detachFromModel() {
		am = null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 73;
		int result = 1;
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((trg == null) ? 0 : trg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Connector other = (Connector) obj;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (trg == null) {
			if (other.trg != null)
				return false;
		} else if (!trg.equals(other.trg))
			return false;
		return true;
	}
	
	
}
