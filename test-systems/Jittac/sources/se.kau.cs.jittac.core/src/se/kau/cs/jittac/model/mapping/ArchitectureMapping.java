package se.kau.cs.jittac.model.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.events.AbstractArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.ComponentRemovalEvent;
import se.kau.cs.jittac.model.mapping.events.ArchitectureMappingChangeEvent;
import se.kau.cs.jittac.model.mapping.events.IArchitectureMappingChangeListener;
import se.kau.cs.jittac.model.mapping.events.ManagedProjectAddedEvent;
import se.kau.cs.jittac.model.mapping.events.ManagedProjectRemovedEvent;
import se.kau.cs.jittac.model.mapping.events.MappingAdditionEvent;
import se.kau.cs.jittac.model.mapping.events.MappingRemovalEvent;

/**
 * An architecture mapping is a mapping between the components of 
 * an architecture model and arbitrary ressources, for example source
 * code files.
 * @author Sebastian Herold
 *
 */
public class ArchitectureMapping {
	
	/**
	 * This class represents single entries in an architecture mapping.
	 * It maps a single component to a single resource.
	 * Entries are organized in a tree. Entries refine their parents. This means
	 * that the resource they refer to is in the scope of the parent entry
	 * and would be mapped to the component referred to there, if the child entry
	 * did not exist. For example, if a source code package pkg is mapped to component
	 * C, also pkg.a is automatically mapped to C, unless there is an entry
	 * mapping pkg.a to another component.
	 * @author Sebastian Herold
	 *
	 */
	public class Entry {
		public final Component comp;
		public final IMappableElement elem;
		public Entry refined = null;
		public Set<Entry> refining = new HashSet<Entry>();
		
		public Entry(Component comp, IMappableElement elem) {
			this.comp = comp;
			this.elem = elem;
		}
	}
	
	private Map<Component, Set<Entry>> explicitMappings;
	private Map<IMappableElement, Entry> explicitlyMappedElements;
	private Map<IJittacProject, Integer> managedProjects;
	private IJittacResourceFactory resourceFactory;
	private List<IArchitectureMappingChangeListener> listeners = new ArrayList<IArchitectureMappingChangeListener>();
	private ArchitectureModel architectureModel;
	
	/**
	 * Creates an empty architecture mapping and sets the
	 * resource factory that should be used for this mapping.
	 * @param resourceFactory The resource factory to be used.
	 * @return A new empty architecture mapping.
	 */
	public static ArchitectureMapping createEmptyMapping(IJittacResourceFactory resourceFactory, ArchitectureModel model) {
		return new ArchitectureMapping(resourceFactory, model);
	}
	
	/**
	 * Create an empty architecture mapping and sets the default
	 * resource factory as to be used.
	 * @return A new empty architecture mapping.
	 */
	public static ArchitectureMapping createEmptyMapping(ArchitectureModel model) {
		return new ArchitectureMapping(JittacResourceModelRegistry.INSTANCE.getDefaultResourceModel(), model);
	}
	
	private ArchitectureMapping(ArchitectureModel model) {
		this(JittacResourceModelRegistry.INSTANCE.getDefaultResourceModel(), model);
	}	
	
	private ArchitectureMapping(IJittacResourceFactory resourceFactory, ArchitectureModel model) {
		if (resourceFactory == null) 
			throw new NullPointerException("No resource model set.");
		explicitMappings = new HashedMap<Component, Set<Entry>>();
		explicitlyMappedElements = new HashedMap<IMappableElement, Entry>();
		managedProjects = new HashedMap<IJittacProject, Integer>();
		this.resourceFactory = resourceFactory;
		this.architectureModel = model;
	}
	
	/**
	 * Returns the resource factory that is used in this mapping.
	 * @return see above.
	 */
	public IJittacResourceFactory getResourceFactory() {
		return resourceFactory;
	}
	
	/**
	 * Determines for resource the closest ancestor resource 
	 * for which an explicit mapping exists.  
	 * @param elem The element for which the described ancestor needs
	 * to be found.
	 * @return The closest explicitly mapped ancestor resource.
	 * If elem itself is explicitly mapped, elem is returned.
	 * Null if no such element exists.
	 */
	public IMappableElement getExplicitlyMappedParent(IMappableElement elem) {
		if (elem == null) return null;
		if (!explicitlyMappedElements.containsKey(elem)) {
			IMappableElement parent = elem.getParent();
			return getExplicitlyMappedParent(parent);
		}
		else {
			return elem;
		}
	}
	
	/**
	 * Determines the component that an element is mapped to, either directly
	 * or indirectly via an ancestor resource.
	 * @param elem The element for which the component is required.
	 * @return The corresponding component if such a component exists. 
	 */
	public Component getComponent(IMappableElement elem) {
		if (elem == null) return null;
		IMappableElement mappedParentOrSelf = this.getExplicitlyMappedParent(elem);
		if (mappedParentOrSelf == null) {
			return null;
		}
		else {
			return explicitlyMappedElements.get(mappedParentOrSelf).comp;
		}
	}

	/**
	 * Returns all the explicitly mapped resources for a component.
	 * @param comp The component for which the mapped resources are requested.
	 * @return Set of mapped resources.
	 */
	public Set<IMappableElement> getMappedResources(Component comp) {
		Set<IMappableElement> result = new HashSet<IMappableElement>();
		if (comp != null) {
			if (explicitMappings.containsKey(comp)) {
				for (Entry e : explicitMappings.get(comp)) {
					result.add(e.elem);
				}
			}
		}
		return result;
	}
	
	/**
	 * Adds a mapping between the specified resource and the specified component.
	 * No mapping is added if
	 * -the component is null or the resource is null, or
	 * -there is an explicit mapping for the resource already, or
	 * -the resource is already indirectly mapped to the specified component (because one its ancestor resources is already mapped to comp).
	 * @param comp The component onto which the resource should be mapped.
	 * @param resource The resource that should be mapped.
	 * @return True if a mapping has been added, false otherwise.
	 */
	public boolean addMapping(Component comp, IMappableElement resource) {
		if (comp == null || resource == null) throw new IllegalArgumentException("Arguments must not be null!");
		if (explicitlyMappedElements.containsKey(resource)) {
			return false;
		}
		else {
			Component currentComponent = getComponent(resource);
			if (comp.equals(currentComponent)) {
				return false;
			}
			else {
				_addMapping(comp, resource);
				fireEvent(new MappingAdditionEvent(comp, resource, currentComponent, this));
				return true;
			}
		}
	}
	
	public boolean removeComponent(Component comp) {
		boolean result = false;
		
		Set<IMappableElement> elements = this.getMappedResources(comp);
		result = elements.size() > 0;
		for (IMappableElement elem : elements) {
			boolean temp = removeMapping(comp, elem, true);
			result = result || temp;
		}
		return result;
	}
	
	/**
	 * Removes a single mapping between a given component and a given resource
	 * if this explicit mapping existed.
	 * @param comp The given component.
	 * @param resource The given resource.
	 * @return True if the the given component was explicitly mapped to the given
	 * resource. False otherwise.
	 */
	public boolean removeMapping(Component comp, IMappableElement resource) {
		return removeMapping(comp, resource, false);
	}

	public Set<IMappableElement> getIdenticallyMappedSubtree(IMappableElement elem) {
		HashSet<IMappableElement> result = new HashSet<>();
		Component comp = this.getComponent(elem);
		if (comp != null) {
			result.add(elem);
			for (IJittacResource child : elem.getChildren()) {
				if (comp.equals(this.getComponent(child))) {
					result.addAll(getIdenticallyMappedSubtree(child));
				}
			}
		}
		return result;
	}
	
	/**
	 * Removes an explicit mapping. See removeMapping(Component, IMappableElement)
	 * @param comp The given component
	 * @param resource The given resource
	 * @param isDuplicateRemoval Indicates whether or not this  removal is
	 * caused by another mapping removal. Removing a mapping entry can cause
	 * a further removals in case it leads to parent and child entries mapping
	 * to the same components which we do not allow. If this parameter is set
	 * to true, mapping listeners are not notified about the change.
	 * @return True if the the given component was explicitly mapped to the given
	 * resource. False otherwise.
	 */
	private boolean removeMapping(Component comp, IMappableElement resource, boolean isDuplicateRemoval) {
		if (comp == null || resource == null) throw new IllegalArgumentException("Arguments must not be null!");
		if (explicitlyMappedElements.containsKey(resource)) {
			Entry removeEntry = explicitlyMappedElements.get(resource);
			Set<Entry> currentMappings = explicitMappings.get(comp);

			_updateRefinementsForRemovedEntry(removeEntry);
			explicitlyMappedElements.remove(resource);
			currentMappings.remove(removeEntry);
			_decreaseResourceCounter(resource.getProject());
			if (currentMappings.isEmpty()) {
				explicitMappings.remove(comp);
			}
			if (!isDuplicateRemoval) {
				fireEvent(new MappingRemovalEvent(comp, resource, this.getComponent(resource), this));
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Adds a listener to the mapping.
	 * @param listener The listener to be added.
	 */
	public void registerListener(IArchitectureMappingChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a listener, so it does not get notified about mapping modifications
	 * anymore.
	 * @param listener The listener to be removed.
	 */
	public void deregisterListener(IArchitectureMappingChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notifies registered listeners about a mapping modification
	 * @param event The mapping modification.
	 */
	public void fireEvent(ArchitectureMappingChangeEvent event) {
		for (IArchitectureMappingChangeListener l : listeners) {
			l.onEvent(event);
		}
	}
	
	private void _deleteEntryFromTree(Entry e) {
		Entry parent = e.refined;
		Set<Entry> children = e.refining;
		for (Entry child : children) {
			child.refined = parent;
		}
		if (parent != null) {
			parent.refining.remove(e);
			parent.refining.addAll(children);
		}
		e.refined = null;
		e.refining.clear();
	}
	
	
	
	private void _addMapping(Component comp, IMappableElement resource) {
		if (comp == null || resource == null) throw new IllegalArgumentException("Arguments must not be null!");		
		Set<Entry> currentMappings;
		
		if (!explicitMappings.containsKey(comp)) {
			currentMappings = new HashSet<Entry>();
			explicitMappings.put(comp, currentMappings);
		}
		else {
			currentMappings = explicitMappings.get(comp);
		}
		
		Entry newEntry = new Entry(comp, resource);
		currentMappings.add(newEntry);
		explicitlyMappedElements.put(resource, newEntry);
		_increaseResourceCounter(resource.getProject());
		_updateRefinementsForNewMapping(newEntry);
	}
	
	private void  _addEntryInTree(Entry newEntry) {
		if (newEntry == null) return;
		Entry parentEntry = explicitlyMappedElements.get(
				this.getExplicitlyMappedParent(newEntry.elem.getParent()));
		newEntry.refined = parentEntry;
		if (parentEntry != null) {
			Set<Entry> temp = new HashSet<Entry>(parentEntry.refining);
			for (Entry child : temp) {
				if (child.elem.isDescendantOf(newEntry.elem)) {
					parentEntry.refining.remove(child);
					child.refined = newEntry;
					newEntry.refining.add(child);
				}
			}
			parentEntry.refining.add(newEntry);
		}
		else {
			for (Entry e : explicitlyMappedElements.values()) {
				if (e.elem.isDescendantOf(newEntry.elem) && e.refined == null) {
					e.refined = newEntry;
					newEntry.refining.add(e);
				}
			}
		}
	}
	
	private void _updateRefinementsForNewMapping(Entry newEntry) {
		if (newEntry == null) return;
		_addEntryInTree(newEntry);
		_deleteIrrelevantRefinement(newEntry);
		_deleteIrrelevantRefinement(newEntry.refined, newEntry);
	}
	
	private void _updateRefinementsForRemovedEntry(Entry removeEntry) {
		if (removeEntry == null) return;
		Entry parent = removeEntry.refined;
		_deleteEntryFromTree(removeEntry);
		_deleteIrrelevantRefinement(parent);
	}
	
	private void _deleteIrrelevantRefinement(Entry entry) {
		if (entry == null) return;
		for (Entry child : entry.refining) {
			_deleteIrrelevantRefinement(entry, child);
		}
	}
	
	private void _deleteIrrelevantRefinement(Entry entry, Entry refining) {
		if (entry == null || refining == null) return;
		if (entry.refining.contains(refining) && entry.comp.equals(refining.comp)) {
			removeMapping(refining.comp, refining.elem, true);
		}
	}
	
	private void _increaseResourceCounter(IJittacProject project) {
		if (project == null) return;
		if (managedProjects.containsKey(project)) {
			int nrOfResources = managedProjects.get(project);
			managedProjects.put(project, nrOfResources + 1);
		}
		else {
			managedProjects.put(project, 1);
			fireEvent(new ManagedProjectAddedEvent(project, this));
		}
	}
	
	private void _decreaseResourceCounter(IJittacProject project) {
		if (project == null) return;
		if (managedProjects.containsKey(project)) {
			int nrOfResources = managedProjects.get(project);
			if (nrOfResources == 1) {
				managedProjects.remove(project);
				fireEvent(new ManagedProjectRemovedEvent(project, this));
			}
			else {
				managedProjects.put(project, nrOfResources - 1);
			}
		}
	}
	
	/**
	 * Returns the mapping entry for a resource
	 * @param elem the resource
	 * @return The mapping entry if an explicit mapping for elem exists.
	 */
	public Entry getEntryForElement(IMappableElement elem) {
		return explicitlyMappedElements.get(elem);
	}
	
	public Set<IJittacProject> getManagedProjects() {
		return new HashSet<IJittacProject>(managedProjects.keySet());
	}
}
