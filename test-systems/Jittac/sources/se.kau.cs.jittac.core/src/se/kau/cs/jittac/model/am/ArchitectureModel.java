package se.kau.cs.jittac.model.am;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.map.HashedMap;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import se.kau.cs.jittac.model.am.events.ArchitectureModelChangeEvent;
import se.kau.cs.jittac.model.am.events.ComponentAdditionEvent;
import se.kau.cs.jittac.model.am.events.ComponentNameChangeEvent;
import se.kau.cs.jittac.model.am.events.ComponentRemovalEvent;
import se.kau.cs.jittac.model.am.events.ConnectorAdditionEvent;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesAddedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesRemovedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorRemovalEvent;
import se.kau.cs.jittac.model.am.events.ConnectorStateChangeEvent;
import se.kau.cs.jittac.model.am.events.IArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.IArchitectureModelVisitor;
import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.IImplementationModelElement;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;
import se.kau.cs.jittac.model.im.events.IImplementationChangeListener;
import se.kau.cs.jittac.model.im.events.XReferenceChangeDeltaEvent;
import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IMappableElement;
import se.kau.cs.jittac.model.mapping.events.AbstractArchitectureMappingChangeListener;
import se.kau.cs.jittac.model.mapping.events.ManagedProjectAddedEvent;
import se.kau.cs.jittac.model.mapping.events.ManagedProjectRemovedEvent;
import se.kau.cs.jittac.model.mapping.events.MappingAdditionEvent;
import se.kau.cs.jittac.model.mapping.events.MappingRemovalEvent;

/**
 * This class represents architecture models, consisting of components and connectors.
 * It implements also IImplementationModelListener functionality to react to any changes
 * occuring in implementation model connected with this architecture model.
 * 
 * It furthermore contains a MappingListener to react to changes in the mapping between
 * components and resources.
 * @author Sebastian Herold
 *
 */
public class ArchitectureModel extends AbstractArchitectureElement
								implements IImplementationChangeListener {

	//Mapping and listener for mapping
	private ArchitectureMapping mapping;
	private MappingListener mappingListener;
	
	//managing listeners to architectural model
	private List<IArchitectureModelChangeListener> modelListeners;
	
	//Components and connectors
	private List<Component> components;
	private List<Connector> connectors;
	
	public static ArchitectureModel createArchitectureModel() {
		return new ArchitectureModel();
	}
	
	private ArchitectureModel() {
		super();
		mappingListener = new MappingListener();
		modelListeners = new ArrayList<IArchitectureModelChangeListener>();
		this.mapping = ArchitectureMapping.createEmptyMapping(this);
		mapping.registerListener(this.mappingListener);
		components = new ArrayList<Component>();
		connectors = new ArrayList<Connector>();
	}
	
	public ArchitectureModel getModel() {
		return this;
	}
	
	@Override
	public String getName() {
		return "Architecture model";
	}
	
	public ArchitectureMapping getMapping() {
		return mapping;
	}
	
	public Component createComponent() {
		return createComponent(null, null);
	}
	
	public Component createComponent(String name) {
		return createComponent(name, null);
	}
	
	public Component createComponent(String name, UUID id) {
		Component newComponent = Component.createComponent(this, id);
		if (newComponent != null) {
			if (name != null) {
				newComponent.setName(name);
			}
			if (!components.contains(newComponent)) {
				components.add(newComponent);
			}
			createConnector(newComponent, newComponent);
		}
		this.fireEvent(new ComponentAdditionEvent(newComponent, this));
		return newComponent;
	}
	
	public Connector createConnector(Component src, Component trg) {
		
		Connector newConnector = Connector.createConnector(src, trg);
		if (newConnector != null && !connectors.contains(newConnector)) {
			connectors.add(newConnector);
			if (src.equals(trg)) {
				src.setReflexiveConnector(newConnector);
			}
			else {
				src.addOutgoingConnector(newConnector);
				trg.addIncomingConnector(newConnector);
				this.fireEvent(new ConnectorAdditionEvent(newConnector, this));
			}
		}
		return newConnector;
	}
	
	public boolean removeComponent(Component comp) {
		if (comp == null) throw new IllegalArgumentException("Argument must not be null");
		if (comp.getModel() != this) throw new IllegalArgumentException("Component " + comp + " already attached to different model.");
		
		Set<Connector> connectorsToBeRemoved = new HashSet<Connector>();
		boolean removed;
		if (components.contains(comp)) {
			for (Connector conn : comp.getOutgoingConnectors()) {
				removed = connectorsToBeRemoved.add(conn);
			}
			for (Connector conn : comp.getIncomingConnectors()) {
				removed = connectorsToBeRemoved.add(conn);
			}
			for (Connector conn : connectorsToBeRemoved) {
				this.removeConnector(conn);
			}
			this.removeConnector(comp.getReflexiveConnector());
			removed = components.remove(comp); 
			if (removed) {
				getMapping().removeComponent(comp);
				comp.detachFromModel();
				this.fireEvent(new ComponentRemovalEvent(comp, this));
			}
			return removed;
		}
		return false;
	}
	
	public boolean removeConnector(Connector conn) {
		if (conn == null) throw new IllegalArgumentException("Argument must not be null");
		if (conn.getModel() != this) throw new IllegalArgumentException("Connector " + conn + " attached to different model.");
		boolean removed;
		
		if (!conn.isReflexive()) {
			conn.getSrc().removeOutgoingConnector(conn);
			conn.getTrg().removeIncomingConnector(conn);
			removed = connectors.remove(conn);
			if (removed) {
				conn.detachFromModel();
				fireEvent(new ConnectorRemovalEvent(conn, this));
			}
		}
		else {
			conn.getSrc().setReflexiveConnector(null);
			removed = connectors.remove(conn);
		}
		return removed;
	}
	
	public void makeConnectorUnenvisaged(Connector conn) {
		if (conn == null) throw new IllegalArgumentException("Argument must not be null");
		if (conn.getModel() != this) throw new IllegalArgumentException("Connector " + conn + " attached to different model.");

		if (conn.getState() == Connector.STATE.CONVERGENCE) {
			changeConnectorState(conn, false);
		}
		else if (conn.getState() == Connector.STATE.DIVERGENCE) {
			
		}
		else if (conn.getState() == Connector.STATE.ABSENCE) {
			removeConnector(conn);
		}
	}
	
	public boolean changeConnectorState(Connector conn, boolean envisaged) {
		if (conn == null) throw new IllegalArgumentException("Argument must not be null");
		if (conn.getModel() != this) throw new IllegalArgumentException("Connector " + conn + " attached to different model.");
		boolean changed = false;
		
		Connector.STATE oldState = conn.getState();
		if (oldState == Connector.STATE.CONVERGENCE ||
			oldState == Connector.STATE.DIVERGENCE) {
			conn.setEnvisaged(envisaged);
			changed = oldState != conn.getState();
		}
		if (changed) {
			this.fireEvent(new ConnectorStateChangeEvent(conn, oldState, conn.getState(), this));
		}
		return changed;
	}
	
	public boolean containsComponent(Component c) {
		return components.contains(c);
	}
	
	public boolean containsConnectorBetween(Component src, Component trg) {
		
		for (Connector c : connectors) {
			if (src.equals(c.getSrc()) && trg.equals(c.getTrg())) {
				return true;
			}
		}
		return false;
	}
	
	public Component getComponentById(UUID id) {
		for (Component c: components) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		return null;
	}
	
	public Set<Component> getComponents() {
		return new HashSet<Component>(this.components);
	}
	
	public Set<Connector> getConnectors() {
		return new HashSet<Connector>(this.connectors);
	}
	
	public List<Component> getComponentsAsList() {
		return new ArrayList<Component>(this.components);
	}

	public List<Connector> getConnectorsAsList() {
		return new ArrayList<Connector>(this.connectors);
	}
	
	public Connector getConnectorByComponents(Component src, Component trg) {

		for (Connector c : connectors) {
			if (src.equals(c.getSrc()) && trg.equals(c.getTrg())) {
				return c;
			}
		}
		return null;
	}

	public Set<IJittacProject> getManagedProjects() {
		return getMapping().getManagedProjects();
	}
	
	public void setComponentName(Component comp, String name) {
		String oldName = comp.getName();
		if (oldName != name) {
			comp.setName(name);
			fireEvent(new ComponentNameChangeEvent(comp, this, oldName, name));
		}
	}
	
	public void registerListener(IArchitectureModelChangeListener listener) {
		if (!modelListeners.contains(listener)) {
			modelListeners.add(listener);
		}
	}
	
	public void deregisterListener(IArchitectureModelChangeListener listener) {
		modelListeners.remove(listener);
	}
	
	@Override
	public void onXReferenceDeltaEvent(XReferenceChangeDeltaEvent event) {
		if (event == null) throw new IllegalArgumentException("Argument must not be null.");

		removeReferencesFromConnectors(event.removedReferences());
		addReferencesToConnectors(event.addedReferences());
		addReferencesToConnectors(event.unchangedReferences());
	}

	@Override
	public void onCompleteLoad(IImplementationModel im) {
		if (this.getManagedProjects().contains(im.getProject())) {
			for (ImplementationModelPartition part : im.getPartitions()) {
				addReferencesToConnectors(part.getReferences());
			}
		}
		
	}
	
	private void fireEvent(ArchitectureModelChangeEvent event) {
		//Local copy to avoid concurrent modification exceptions
		List<IArchitectureModelChangeListener> temp = new ArrayList<>(modelListeners);
		for (IArchitectureModelChangeListener listener : temp) {
			listener.handleEvent(event);
		}
	}
	
	private boolean addReferencesToConnectors(Set<? extends IXReference<?,?>> refs) {
		if (refs == null) throw new IllegalArgumentException("Argument must not be null.");
		boolean referenceSetModified = false;
		boolean result = false;
		Map<Connector, Set<IXReference<?,?>>> mappings = new HashedMap<Connector, Set<IXReference<?,?>>>();
		
		for (IXReference<?,?> ref : refs) {
			Component srcComp = 
					mapping.getComponent(ref.getSource().getResource());
			Component trgComp = 
					mapping.getComponent(ref.getTarget().getResource());
			if (srcComp != null && trgComp != null /*&& !srcComp.equals(trgComp)*/) {
				Connector conn = this.getConnectorByComponents(srcComp, trgComp);
				if (conn == null) {
					conn = this.createConnector(srcComp, trgComp);
					conn.setEnvisaged(false);
				}
				if (!mappings.containsKey(conn)) {
					mappings.put(conn, new HashSet<IXReference<?,?>>());
				}
				mappings.get(conn).add(ref);
			}
		}
		for (Connector changedConnector : mappings.keySet()) {
			Set<IXReference<?,?>> actuallyNewReferences = mappings.get(changedConnector);
			actuallyNewReferences.removeAll(changedConnector.getContributingReferences());
					
			referenceSetModified = changedConnector.addReferences(mappings.get(changedConnector));
			result = result || referenceSetModified;
			if (!changedConnector.isReflexive() && referenceSetModified)
				fireEvent(new ConnectorReferencesAddedEvent(changedConnector, actuallyNewReferences, this));
		}
		return result;
	}
	
	/**
	 * Do not call when mapping is in consistent state, e.g. while handling mapping change events.
	 * @param refs
	 * @return
	 */
	private boolean removeReferencesFromConnectors(Set<IXReference<?,?>> refs) {
		if (refs == null) throw new IllegalArgumentException("Argument must not be null.");
		
		Map<Connector, Set<IXReference<?,?>>> mappings = new HashedMap<Connector, Set<IXReference<?,?>>>();
		boolean referenceSetModified = false;
		boolean result = false;
		
		for (IXReference<?,?> ref : refs) {
			Component srcComp = 
					mapping.getComponent(ref.getSource().getResource());
			Component trgComp = 
					mapping.getComponent(ref.getTarget().getResource());
			Connector conn = getConnectorByComponents(srcComp, trgComp);
			
			if (conn != null) {
				if (!mappings.containsKey(conn)) {
					mappings.put(conn, new HashSet<IXReference<?,?>>());
				}
				mappings.get(conn).add(ref);
			}
		}
		for (Connector conn : mappings.keySet()) {
			/*Set<IXReference> actuallyRemovedRefs = conn.getContributingReferences();
			Connector.STATE oldState = conn.getState();
			actuallyRemovedRefs.retainAll(mappings.get(conn));
			
			referenceSetModified = conn.removeReferences(actuallyRemovedRefs);*/
			result = result || removeReferencesFromConnector(conn, mappings.get(conn));
/*			if (!conn.isReflexive() && referenceSetModified) {
				fireEvent(new ConnectorReferencesRemovedEvent(conn, actuallyRemovedRefs, this));
			}
			//The following is true if we removed the last references of a connector
			if (conn.getState() != oldState ) {
				//The following is true if we removed the last references of a divergence
				if (conn.getState() == Connector.STATE.INCONSISTENT) {
					this.removeConnector(conn);
				}
				else {
					fireEvent(new ConnectorStateChangeEvent(conn, oldState, conn.getState(), this));
				}
			}*/
		}
		return referenceSetModified;
	}
	
	private boolean removeReferencesFromConnector(Connector conn, Set<IXReference<?,?>> refs) {
		boolean result = false;
		
		if (conn != null && refs != null) {
			Set<IXReference<?,?>> actuallyRemovedRefs = conn.getContributingReferences();
			actuallyRemovedRefs.retainAll(refs);
			Connector.STATE oldState = conn.getState();
			result = conn.removeReferences(actuallyRemovedRefs);
			if (!conn.isReflexive() && result) {
				fireEvent(new ConnectorReferencesRemovedEvent(conn, actuallyRemovedRefs, this));
				if (conn.getState() != oldState ) {
					//The following is true if we removed the last references of a divergence
					if (conn.getState() == Connector.STATE.INCONSISTENT) {
						this.removeConnector(conn);
					}
					else {
						fireEvent(new ConnectorStateChangeEvent(conn, oldState, conn.getState(), this));
					}
				}
			}
		}
		
		return result;
	}
	
	private class MappingListener extends AbstractArchitectureMappingChangeListener {

		@Override
		public void onMappingAdded(MappingAdditionEvent event) {
			IMappableElement newlyMappedResource = event.getElement();
			Map<Connector, Set<IXReference<?,?>>> relevantRefs = new HashedMap<Connector, Set<IXReference<?,?>>>();
			Set<IXReference<?,?>> formerlyUnmappedReferences = new HashSet<IXReference<?,?>>();
			
			if (event.getPreviousComponent() != null) {
				Set<Connector> connectors = event.getPreviousComponent().getOutgoingConnectors();
				connectors.addAll(event.getPreviousComponent().getIncomingConnectors());
				connectors.add(event.getPreviousComponent().getReflexiveConnector());
				for (Connector con : connectors) {
					relevantRefs.put(con, _getReferencesAffectedByChangeOfMapping(con, newlyMappedResource, event.getPreviousComponent()));
				}
			}
			else {
				//retrieve references from implementation model
				_gatherReferencesFromIdenticallyMappedChildResources(event.getElement(), formerlyUnmappedReferences);
				
			}
			//remove refs and attach to correct connectors
			for (Connector conn : relevantRefs.keySet()) {
				//TODO: add a corresponding removeReferences to this class.
				//conn.removeReferences(relevantRefs.get(conn));
				//fireEvent(new ConnectorReferencesRemovedEvent(conn, relevantRefs.get(conn), ArchitectureModel.this));
				removeReferencesFromConnector(conn, relevantRefs.get(conn));

				formerlyUnmappedReferences.addAll(relevantRefs.get(conn));
				//addReferencesToConnectors();
			}
			addReferencesToConnectors(formerlyUnmappedReferences);
			event.getElement().getProject().getImplementationModel().
				registerImplementationChangeListener(ArchitectureModel.this);
		}

		@Override
		public void onMappingRemoved(MappingRemovalEvent event) {
			IMappableElement newlyMappedResource = event.getElement();
			Map<Connector, Set<IXReference<?,?>>> relevantRefs = new HashedMap<Connector, Set<IXReference<?,?>>>();
			//get all connectors that go to old component;
			Set<Connector> connectors = event.getComponent().getOutgoingConnectors();
			Set<IXReference<?,?>> refsToBeRemoved = new HashSet<IXReference<?,?>>();
			connectors.addAll(event.getComponent().getIncomingConnectors());
			connectors.add(event.getComponent().getReflexiveConnector());
			for (Connector con : connectors) {
				relevantRefs.put(con, _getReferencesAffectedByChangeOfMapping(con, newlyMappedResource, event.getComponent()));
			}
			for (Connector con : relevantRefs.keySet()) {
				removeReferencesFromConnector(con, relevantRefs.get(con));
				refsToBeRemoved.addAll(relevantRefs.get(con));
			}
			if (event.getComponentAfterRemoval() != null) {
				addReferencesToConnectors(refsToBeRemoved);
			}
			if (!getMapping().getManagedProjects().contains(event.getElement().getProject())) {
				event.getElement().getProject().getImplementationModel().
					deregisterImplementationChangeListener(ArchitectureModel.this);
			}
		}

		@Override
		public void onManagedProjectAdded(ManagedProjectAddedEvent event) {
			IJittacProject project = event.getProject();
			IImplementationModel im = project.getImplementationModel();
			im.registerImplementationChangeListener(ArchitectureModel.this);
			ArchitectureModel.this.onCompleteLoad(im);
		}
		
		public void onManagedProjectRemoved(ManagedProjectRemovedEvent event) {
			IJittacProject project = event.getProject();
			IImplementationModel im = project.getImplementationModel();
			im.deregisterImplementationChangeListener(ArchitectureModel.this);
		}		
		
		/**
		 * Queries references of a single connector that are need to be re-routed, i.e. to be assigned to a
		 * different connector because the mapping of the given element has changed.
		 * @param con The connector for which the references are scanned
		 * @param elem The element for which the mapping was changed
		 * @param prevCompForElem The component that the element was previously mapped to.
		 * @return The set of references that are affected. 
		 */
		private Set<IXReference<?,?>> _getReferencesAffectedByChangeOfMapping(Connector con, 
				IMappableElement elem, Component prevCompForElem) {
			Set<IXReference<?,?>> relevantRefs = new HashSet<IXReference<?,?>>();
			IMappableElement endPointAResource = null;
			IMappableElement endPointBResource = null;
			
			for (IXReference<?,?> ref : con.getContributingReferences()) {
				if (con.isReflexive()) {
					endPointAResource = mapping.getExplicitlyMappedParent(ref.getSource().getResource());
					endPointBResource = mapping.getExplicitlyMappedParent(ref.getTarget().getResource());
				}
				else if (con.getSrc().equals(prevCompForElem)) {
					endPointAResource = mapping.getExplicitlyMappedParent(ref.getSource().getResource());
					endPointBResource = endPointAResource;
				}
				else  if (con.getTrg().equals(prevCompForElem)) {
					endPointBResource = mapping.getExplicitlyMappedParent(ref.getTarget().getResource());
					endPointAResource = endPointBResource;
				}
				elem = mapping.getExplicitlyMappedParent(elem);		
				if (elem == null) {
					if (endPointAResource == null || endPointBResource == null) {
					relevantRefs.add(ref);
					}
				}
				else if (elem.equals(endPointAResource) || elem.equals(endPointBResource)) {
					relevantRefs.add(ref);
				}
			}
			return relevantRefs;
		}
		
		private void _gatherReferencesFromIdenticallyMappedChildResources(IMappableElement elem, Set<IXReference<?,?>> result) {
			IImplementationModel im = elem.getProject().getImplementationModel();
			Set<IXReference<? extends IImplementationModelElement<?>,?>> refsInResource =  new HashSet<>();
			refsInResource.addAll(im.getReferencesForResource(elem));
			refsInResource.addAll(im.getIncomingReferencesForResource(elem));
			result.addAll(refsInResource);
			for (IMappableElement child : elem.getChildren()) {
				if (mapping.getComponent(child).equals(mapping.getComponent(elem))) {
					_gatherReferencesFromIdenticallyMappedChildResources(child, result);
				}
			}
		}
	}
}
