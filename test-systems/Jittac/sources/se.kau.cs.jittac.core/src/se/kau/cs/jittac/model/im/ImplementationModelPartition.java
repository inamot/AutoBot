package se.kau.cs.jittac.model.im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.kau.cs.jittac.model.im.events.BuildEndEvent;
import se.kau.cs.jittac.model.im.events.BuildStartEvent;
import se.kau.cs.jittac.model.mapping.IJittacResource;

public class ImplementationModelPartition {

	public static enum State {
		INIT, BUILDING, IDLE;
	}
	
	private IImplementationModel im;
	private ImplementationModelPartitionType type;
	private Map<IJittacResource, BuildUnit> buildUnits;
	private List<BuildUnit> currentUnits;
	private State state;
	
	public ImplementationModelPartition(IImplementationModel im, ImplementationModelPartitionType type) {
		this.im = im;
		this.type = type;
		currentUnits = new ArrayList<>();
		state = State.IDLE;
		buildUnits = new HashMap<>();
	}
	
	public synchronized State startBuild() {
		if (state == State.IDLE) {
			state = State.BUILDING;
			im.fireBuildEvent(new BuildStartEvent(this));
		}
		else {
			//TODO: exception?
		}
		return state;
	}
	
	public synchronized State endBuild() {
		if (state == State.BUILDING) {
			im.fireBuildEvent(new BuildEndEvent(this));
			state = State.IDLE;
		}
		else {
			//TODO: exception?
		}
		return state;
	}
	
	public synchronized State startInit() {
		if (state == State.IDLE) {
			state = State.INIT;
		}
		else {
			//TODO: exception?
		}
		return state;
	}
	
	public synchronized State endInit() {
		if (state == State.INIT) {
			state = State.IDLE;
		} 
		else {
			//TODO: exception?
		}
		return state;
	}
	
	public synchronized BuildUnit startUnit(IJittacResource resourceOfUnit) {
		if (state == State.BUILDING) {
			BuildUnit unit = getUnit(resourceOfUnit);
			if (!currentUnits.contains(unit)) {
				currentUnits.add(unit);
				unit.start();
			}
			return unit;
		}
		return null;
	}
	
	public synchronized void endUnit(BuildUnit unit)  {
		
		if (currentUnits.contains(unit) && state == State.BUILDING) {
			unit.end();
			currentUnits.remove(unit);
		}
	}
	
	public void clearUnit(BuildUnit unit) {
		unit.clear();
	}

	public IImplementationModel getImplementationModel() {
		return im;
	}

	public ImplementationModelPartitionType getBuilderType() {
		return type;
	}
	
	public synchronized void addReference(IXReference<? extends IImplementationModelElement<?>,?> ref) {
		BuildUnit unit = this.getUnit(ref.getResource());
		if (state == State.BUILDING) {
			unit.addXReference(ref);
		}
		else if (state == State.INIT) {
			unit.loadReference(ref);
		}
	}
	
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>> getReferences(IJittacResource res) {
		if (buildUnits.containsKey(res)) {
			return buildUnits.get(res).getReferences();
		}
		else {
			return new HashSet<IXReference<?,?>>();
		}
	}
	
	public Set<? extends IXReference<? extends IImplementationModelElement<?>,?>>
	getIncomingReferences(IJittacResource resource) {
		if (buildUnits.containsKey(resource)) {
			return buildUnits.get(resource).getIncomingReferences();
		}
		else {
			return new HashSet<IXReference<?,?>>();
		}

	}
	
	public Set<? extends IXReference<?,?>> getReferences() {
		//Set<IXReference<?,?>> result = new HashSet<IXReference<?,?>>();
		Set<IXReference<? extends IImplementationModelElement<?>,?>> result = new HashSet<>();
		for (BuildUnit unit : buildUnits.values()) {
			result.addAll(unit.getReferences());
		}
		return result;
	}
	
	public BuildUnit getUnit(IJittacResource resourceOfUnit) {
		BuildUnit unit = buildUnits.get(resourceOfUnit);
		if (unit == null) {
			unit = new BuildUnit(resourceOfUnit, this);
			buildUnits.put(resourceOfUnit, unit);
		}
		return unit;
	}
}
