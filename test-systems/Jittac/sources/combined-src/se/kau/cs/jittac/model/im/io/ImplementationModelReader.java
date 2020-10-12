package se.kau.cs.jittac.model.im.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.ImplementationModelFactory;
import se.kau.cs.jittac.model.im.ImplementationModelFactoryRegistry;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;

/**
 * Very primitive implementation - might need redesign soon
 * Lines either start with <INTERNAL> or a partition type identifier
 * @author sebahero
 *
 */
public class ImplementationModelReader {
	

	public static void read(InputStream input, IImplementationModel model) {
		String line;
		ImplementationModelFactory<?,?,?,?> factory = null;
		ImplementationModelPartition partition = null;
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("\"<INTERNAL>\"")) {
					if (factory != null && partition != null) {
						IXReference<?,?> ref = factory.deserializeInternalReference(line, partition);
						partition.addReference(ref);
					}
				}
				else {
					if (partition != null) {
						partition.endInit();
					}
					factory = ImplementationModelFactoryRegistry.instance().getFactory(line);
					partition = model.getPartitionForBuilderType(factory.getPartitionType());
					if (partition != null) {
						partition.startInit();
					}
				}
			}
			if (partition != null) {
				partition.endInit();
			}
			//TODO: The following only works for one-partition projects because the event is raised
			//after each partition (instead of once after the complete model). To be changed.
			model.fireCompleteLoad();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
