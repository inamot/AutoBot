package se.kau.cs.jittac.model.am;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Class representing components of architecture models. Components ("boxes") in architecture models
 * represent logical partitions of source code. The mapping between components and source code
 * is managed by se.kau.cs.jittac.model.mapping.ArchitectureMapping which, in turn, is accessed via
 * the component's architecture model.
 *  
 * @author Sebastian Herold
 *
 */
public class Component extends AbstractArchitectureElement {

	private String name = "Unnamed component";
	private ArchitectureModel am;
	
	private Set<Connector> outgoingConnectors;
	private Set<Connector> incomingConnectors;
	private Connector reflexiveConnector;
	
	/**
	 * Creates a component inside the given architecture model.
	 * @param am The architecture model that should contain the new component.
	 * @return The new component.
	 */
	public static Component createComponent(ArchitectureModel am) {
		return new Component(am);
	}
	
	/**
	 * Creates a component with the given id. ONLY TO BE CALLED DURING DESERIALIZATION!
	 * @param am The architecture model that should contain the new component.
	 * @param id The id assigned to the new component.
	 * @return The new component.
	 */
	public static Component createComponent(ArchitectureModel am, UUID id) {
		return new Component(am, id);
	}
	
	private Component(ArchitectureModel am, UUID id) {
		super(id);
		this.am = am;
		outgoingConnectors = new HashSet<Connector>();
		incomingConnectors = new HashSet<Connector>();
		reflexiveConnector = null;
	}
	
	private Component(ArchitectureModel am) {
		//pre: am != null
		this(am, null);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ArchitectureModel getModel() {
		return am;
	}

	/**
	 * Add a new outgoing connector to the component.
	 * @param conn The connector to be added.
	 * @return True if connector is truly added, i.e. it had not been connected as outgoing from the component before.
	 */
	public boolean addOutgoingConnector(Connector conn) {
		return outgoingConnectors.add(conn);
	}

	/**
	 * Add a new incoming connector to the component.
	 * @param conn The connector to be added.
	 * @return True if connector is truly added, i.e. it had not been connected as incoming to the component before.
	 */
	public boolean addIncomingConnector(Connector conn) {
		return incomingConnectors.add(conn);
	}
	
	/**
	 * Sets the components reflexive connector which connects the components with itself.
	 * This is a supporting construct that makes dependency analysis easier. The reflexive
	 * connector is NOT counted as a incoming or outgoing connector.
	 * @param conn The connector supposed to be the reflexive connector.
	 */
	public void setReflexiveConnector(Connector conn) {
		this.reflexiveConnector = conn;
	}
	
	/**
	 * Removes a connector from the component's set of outgoing connectors.
	 * @param conn The connector to be removed
	 * @return True if the connector was in the set before, false otherwise.
	 */
	public boolean removeOutgoingConnector(Connector conn) {
		return outgoingConnectors.remove(conn);
	}

	/**
	 * Removes a connector from the component's set of incoming connectors.
	 * @param conn The connector to be removed
	 * @return True if the connector was in the set before, false otherwise.
	 */
	public boolean removeIncomingConnector(Connector conn) {
		return incomingConnectors.remove(conn);
	}	
	
	/**
	 * Returns the component's set of outgoing connectors.
	 * @return See above. Empty set if there are no outgoing connectors.
	 */
	public Set<Connector> getOutgoingConnectors() {
		return new HashSet<Connector>(this.outgoingConnectors);
	}

	/**
	 * Returns the component's set of incoming connectors.
	 * @return See above. Empty set if there are no incoming connectors.
	 */

	public Set<Connector> getIncomingConnectors() {
		return new HashSet<Connector>(this.incomingConnectors);
	}

	/**
	 * Gets the components reflexive connector which connects the components with itself.
	 * This is a supporting construct that makes dependency analysis easier. The reflexive
	 * connector is NOT counted as a incoming or outgoing connector.
	 * @return The reflexive connector of the component.
	 */
	public Connector getReflexiveConnector() {
		return this.reflexiveConnector;
	}
	
	public void detachFromModel() {
		am = null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 67;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		Component other = (Component) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	
}
