package se.kau.cs.jittac.model.im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.kau.cs.jittac.model.im.events.BuildEvent;
import se.kau.cs.jittac.model.im.events.BuildEventListener;
import se.kau.cs.jittac.model.im.events.IImplementationChangeListener;
import se.kau.cs.jittac.model.im.events.XReferenceChangeDeltaEvent;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IMappableElement;

public class ImplementationModel implements IImplementationModel {

	private IJittacProject project;
	private List<IImplementationChangeListener> changeListeners = new ArrayList<IImplementationChangeListener>();
	private List<BuildEventListener> buildEventListeners = new ArrayList<BuildEventListener>();
	
	private Map<ImplementationModelPartitionType, ImplementationModelPartition> partitionsMap = new HashMap<>();

	public ImplementationModel(IJittacProject project) {
		this.project = project;
	}

	@Override
	public IJittacProject getProject() {
		return this.project;
	}

	@Override
	public Set<ImplementationModelPartition>
	getPartitions() {
		return new HashSet<>(partitionsMap.values());
	}

	@Override
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResource(IMappableElement elem) {
		//Set<IXReference<?,?>> result = new HashSet<>();
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		//We don't allow references directly at project level, it's always resources
		if (elem instanceof IJittacResource) {
			IJittacResource res = (IJittacResource) elem;
			//TODO: better throw exception if elem is not in the model's project
			if (!res.getProject().equals(getProject())) return result;
			for (ImplementationModelPartition part : getPartitions()) {
				result.addAll(part.getReferences(res));
			}
		}
		return result;
	}

	@Override
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResource(IMappableElement elem,	ImplementationModelPartitionType builderType) {
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		if (elem instanceof IJittacResource) {
			IJittacResource res = (IJittacResource) elem;
			//TODO: better throw exception if elem is not in the model's project
			if (!elem.getProject().equals(getProject())) return result;
			ImplementationModelPartition part = getPartitionForBuilderType(builderType);
			result.addAll(part.getReferences(res));
		}
		return result;
	}

	@Override
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResourceTree(IMappableElement elem) {
		//TODO: better throw exception if elem is not in the model's project
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		if (!elem.getProject().equals(getProject())) return result;
		result.addAll(getReferencesForResource(elem));
		for (IJittacResource child : elem.getChildren()) {
			result.addAll(getReferencesForResourceTree(child));
		}
		return result;
	}

	@Override
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getReferencesForResourceTree(
			IMappableElement elem, ImplementationModelPartitionType builderType) {
		//TODO: better throw exception if elem is not in the model's project
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		if (!elem.getProject().equals(getProject())) return result;
		result.addAll(getReferencesForResource(elem, builderType));
		for (IJittacResource child : elem.getChildren()) {
			result.addAll(getReferencesForResourceTree(child, builderType));
		}
		return result;

	}

	@Override
	public 	ImplementationModelPartition getPartitionForBuilderType(ImplementationModelPartitionType type) {
		
		if (partitionsMap.containsKey(type)) {
			return partitionsMap.get(type);
		}
		else {
			ImplementationModelPartition newPartition = createPartition(type);
			partitionsMap.put(type, newPartition);
			return newPartition;
		}

	}

	@Override
	public void registerImplementationChangeListener(IImplementationChangeListener listener) {
		if (!changeListeners.contains(listener)) {
			changeListeners.add(listener);
		}
	}

	@Override
	public void deregisterImplementationChangeListener(IImplementationChangeListener listener) {
		changeListeners.remove(listener);
	}

	@Override
	public void fireChangeEvent(XReferenceChangeDeltaEvent event) {
		for (IImplementationChangeListener l : changeListeners) {
			l.onXReferenceDeltaEvent(event);
		}
	}
	
	protected ImplementationModelPartition createPartition(ImplementationModelPartitionType type) {
		return new ImplementationModelPartition(this,type);
	}

	@Override
	public void registerBuildEventListener(BuildEventListener listener) {
		if (!buildEventListeners.contains(listener)) {
			buildEventListeners.add(listener);
		}
		
	}

	@Override
	public void deregisterBuildEventListener(BuildEventListener listener) {
		buildEventListeners.remove(listener);
	}

	@Override
	public void fireBuildEvent(BuildEvent event) {
		for (BuildEventListener l : buildEventListeners) {
			l.onBuildEvent(event);
		}
	}

	@Override
	public void fireCompleteLoad() {
		for (IImplementationChangeListener l : changeListeners) {
			l.onCompleteLoad(this);
		}
	}

	@Override
	public Set<? extends IXReference<? extends IImplementationModelElement<?>, ?>> getIncomingReferencesForResource(
			IMappableElement elem) {
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		for (ImplementationModelPartition part : this.getPartitions()) {
			result.addAll(getIncomingReferencesForResource(elem, part.getBuilderType()));
		}
		return result;
	}

	@Override
	public Set<? extends IXReference<? extends IImplementationModelElement<?>, ?>> getIncomingReferencesForResource(
			IMappableElement elem, ImplementationModelPartitionType type) {
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		if (elem instanceof IJittacResource) {
			IJittacResource res = (IJittacResource) elem;
			//TODO: better throw exception if elem is not in the model's project
			if (!elem.getProject().equals(getProject())) return result;
			ImplementationModelPartition part = getPartitionForBuilderType(type);
			result.addAll(part.getIncomingReferences(res));
		}
		return result;
	}
}
