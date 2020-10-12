package se.kau.cs.jittac.model.im;

import java.util.HashMap;

public class ImplementationModelFactoryRegistry {
	
	private static ImplementationModelFactoryRegistry INSTANCE =
			new ImplementationModelFactoryRegistry();
	
	@SuppressWarnings("rawtypes")
	HashMap<String, ImplementationModelFactory> factories =
			new HashMap<String, ImplementationModelFactory>();
	
	
	public  <T extends IImplementationModelElement<E>,E, U extends IXReference<T,F>,F>
	boolean register(ImplementationModelFactory<T,E,U,F> factory) {
		String typeName = factory.getPartitionType().getName();
		if (factories.containsKey(typeName)) {
			return false;
		}
		else {
			factories.put(typeName, factory);
			return true;
		}
	}

	public 	void deregister(ImplementationModelPartitionType type) {
		factories.remove(type.getName());
	}
	
	public  <T extends IImplementationModelElement<E>,E, U extends IXReference<T,F>,F>
	ImplementationModelFactory<T,E,U,F> getFactory(ImplementationModelPartitionType type) {
		return getFactory(type.getName());
	}
	
	public  <T extends IImplementationModelElement<E>,E, U extends IXReference<T,F>,F>
	ImplementationModelFactory<T,E,U,F> getFactory(String typeName) {
		return factories.get(typeName);
	}
	
	public static ImplementationModelFactoryRegistry instance() {
		return INSTANCE;
	}
}
