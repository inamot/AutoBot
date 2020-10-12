package se.kau.cs.jittac.model.mapping;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

/**
 * This class provides the means for managing different resource models,
 * e.g. for different IDEs.
 * 
 * @author Sebastian Herold
 *
 */
public class JittacResourceModelRegistry {

	private Map<String, IJittacResourceFactory> map;
	private IJittacResourceFactory defaultModel = null;
	
	public static JittacResourceModelRegistry INSTANCE = new JittacResourceModelRegistry();
	
	private JittacResourceModelRegistry() {
		map = new HashedMap<String, IJittacResourceFactory>();
	}
	
	/**
	 * Registers a new resource model by a factory for that resource model.
	 * @param factory The resource factory. getResourceModelName for that factory
	 * must not be empty. Will only be registered if no other factory for the same
	 * name has been registered before.
	 */
	public void registerResourceModel(IJittacResourceFactory factory) {
		String name = factory.getResourceModelName();
		if (name == null || name.equals("") || factory == null) throw new IllegalArgumentException("Cannnot register resource model.");

		if (!containsFactoryFor(name)) {
			map.put(name, factory);
		}
	}
	
	/**
	 * Sets the default resource model/factory.
	 * @param factory The factory to be used as default
	 * @return True if resource model/factory was registered. False otherwise.
	 */
	public boolean setDefaultResourceModel(IJittacResourceFactory factory) {
		if (map.containsKey(factory.getResourceModelName())) {
			defaultModel = factory;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the factory for the default resource model.
	 * @return
	 */
	public IJittacResourceFactory getDefaultResourceModel() {
		return defaultModel;
	}
	
	/**
	 * Checks whether a factory for a resource model exists.
	 * @param modelName The resource model given by its name
	 * @return True if factory exists, false otherwise.
	 */
	public boolean containsFactoryFor(String modelName) {
		return map.containsKey(modelName);
	}
	
	/**
	 * Returns the factory for a given resource model name.
	 * @param modelName The resource model name.
	 * @return see above.
	 */
	public IJittacResourceFactory getResourceModelFor(String modelName) {
		return map.get(modelName);
	}
	
	/**
	 * Deregisters a resource model by its name.
	 * @param modelName the name.
	 */
	public void deregisterResourceModel(String modelName) {
		map.remove(modelName);
	}
}
