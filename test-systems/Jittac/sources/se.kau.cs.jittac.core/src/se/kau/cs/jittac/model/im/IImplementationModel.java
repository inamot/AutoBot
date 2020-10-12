package se.kau.cs.jittac.model.im;

import java.util.Set;

import se.kau.cs.jittac.model.im.events.BuildEvent;
import se.kau.cs.jittac.model.im.events.BuildEventListener;
import se.kau.cs.jittac.model.im.events.IImplementationChangeListener;
import se.kau.cs.jittac.model.im.events.XReferenceChangeDeltaEvent;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IMappableElement;

/**
 * Interface for implementation models.
 * These must consist of (at least) a collection of references (IXReference).
 * They are populated by implementation model builders.
 * Each IJittacProject is associated with exactly one implementation model,
 * meaning that a singe implementation model can be populated by different
 * builders (e.g., one for Java source code, one interpreting Spring XML files).
 * 
 * @author Sebastian Herold
 *
 */
public interface IImplementationModel {
	
	/**
	 * Returns the project that this model belongs to.
	 * @return see above.
	 */
	public IJittacProject getProject();

	public Set<ImplementationModelPartition> getPartitions();
	
	/**
	 * Retrieves all references in the implementation model that are contained in the given resource.
	 * @param elem The resources for which the references are returned.
	 * @return The set of references if any, an empty set if there are no references for the resource.
	 */
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResource(IMappableElement elem);
	
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResource(IMappableElement elem, ImplementationModelPartitionType type);

	/**
	 * Retrieves all references in the implementation model that are contained in the given resource or its subresources.
	 * @param elem The resources for which the references are returned.
	 * @return The set of references if any, an empty set if there are no references for the resource.
	 */
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResourceTree(IMappableElement elem);
	
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResourceTree(IMappableElement elem, ImplementationModelPartitionType type);

	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getIncomingReferencesForResource(IMappableElement elem);
	
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getIncomingReferencesForResource(IMappableElement elem, ImplementationModelPartitionType type);
	
	public ImplementationModelPartition getPartitionForBuilderType(ImplementationModelPartitionType type);
	
	
	/**
	 * Registers  a listener with the implementation model that will
	 * be notified in case of modifications in the implementation model.
	 * @param listener The listener to be added.
	 */
	public void registerImplementationChangeListener(IImplementationChangeListener listener);
	
	/**
	 * Deregisters a listener. No effect if the given listener is not registered.
	 * @param listener The listener to be removed.
	 */
	public void deregisterImplementationChangeListener(IImplementationChangeListener listener);
	
	public void fireChangeEvent(XReferenceChangeDeltaEvent event);
	
	public void registerBuildEventListener(BuildEventListener listener);
	
	public void deregisterBuildEventListener(BuildEventListener listener);
	
	public void fireBuildEvent(BuildEvent event);
	
	public void fireCompleteLoad();
}
