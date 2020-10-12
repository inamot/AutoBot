package se.kau.cs.jittac.model.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;

import se.kau.cs.jittac.model.mapping.IJittacResource;

public class FeatureLocationRegistry {
	
	public static FeatureLocationRegistry INSTANCE = new FeatureLocationRegistry();
	
	private Map<IJittacResource, Set<FeatureLocation>> locations; 
	private Map<String, Feature> instantiatedFeatures;
	
	
	private FeatureLocationRegistry() {
		locations = new HashedMap<>();
		instantiatedFeatures = new HashedMap<>();
	}
	
	public Set<Feature> getFeatures(IJittacResource resource) {
		Set<Feature> result = new HashSet<>();
		if (locations.containsKey(resource)) {
			for (FeatureLocation fl : locations.get(resource)) {
				result.add(fl.getFeature());
			}
		}
		return result;
	}
	
	public Set<FeatureLocation> getFeatureLocations(IJittacResource resource) {
		if (locations.containsKey(resource)) {
			return locations.get(resource); 
		}
		else {
			return new HashSet<FeatureLocation>();
		}
	}
	
	public List<FeatureLocation> getAllFeatureLocations()
	{
		List<FeatureLocation> allFeatLocs = new ArrayList<FeatureLocation>();;
		
		for( Set<FeatureLocation> featLocs : locations.values() )
		{
			allFeatLocs.addAll( featLocs );
		}
		
		return allFeatLocs; 
	}
	
	public Feature getFeature(IJittacResource resource, int position) {
		Set<FeatureLocation> locations = this.getFeatureLocations(resource);
		for (FeatureLocation fl : locations) {
			if (fl.getOffset() <= position && fl.getOffset() + fl.getLength() >= position) {
				return fl.getFeature();
			}
		}
		return null;
	}
	
	public void registerFeatureLocation(FeatureLocation location) {
		IJittacResource res = location.getResource();
		if (!locations.containsKey(res)) {
			locations.put(res, new HashSet<>());
		}
		locations.get(res).add(location);
	}
	
	public void clear(IJittacResource resource) {
		if (locations.containsKey(resource)) {
			locations.get(resource).clear();			
		}
	}

	public void clearAll()
	{	
		locations.clear();
	}

}