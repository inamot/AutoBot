package se.kau.cs.jittac.model.feature;

import se.kau.cs.jittac.model.mapping.IJittacResource;

public class FeatureLocation {

	private IJittacResource resource;
	private Feature feature;
	private int offset;
	private int length;
	
	public FeatureLocation(Feature feature, IJittacResource resource, int offset, int length) {
		this.resource = resource;
		this.feature = feature;
		this.offset = offset;
		this.length = length;
	}

	public IJittacResource getResource() {
		return resource;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public Feature getFeature() {
		return feature;
	}
	
	public String toString() {
		return feature.getName() + ", " + resource.toString() + ", " + offset + ", " + length; 
	}
}
