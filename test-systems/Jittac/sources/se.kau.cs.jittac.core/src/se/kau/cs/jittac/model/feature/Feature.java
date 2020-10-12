package se.kau.cs.jittac.model.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Feature {

	public static Map<String, Feature> instances = new HashMap<String, Feature>();
	
	private String name;
	
	private Set<FeatureLocation> locations;

	private Feature(String name) {
		this.name = name;
		locations = new HashSet<>();
	}
	
	public static Feature getFeature(String name) {
		if (instances.containsKey(name)) {
			return instances.get(name);
		}
		else {
			Feature newFeature = new Feature(name);
			instances.put(name, newFeature);
			return newFeature;
		}
	}
	
	public static void removeFeature(String name) {
		instances.remove(name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<FeatureLocation> getLocations() {
		return new HashSet<>(locations);
	}

	public void addLocation(FeatureLocation location) {
		locations.add(location);
	}
	
	public void removeLocation(FeatureLocation location) {
		locations.remove(location);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Feature other = (Feature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
