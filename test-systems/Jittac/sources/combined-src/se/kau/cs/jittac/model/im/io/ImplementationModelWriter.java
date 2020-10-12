package se.kau.cs.jittac.model.im.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.IImplementationModelElement;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.ImplementationModelFactory;
import se.kau.cs.jittac.model.im.ImplementationModelFactoryRegistry;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;

public class ImplementationModelWriter {
	
	public static void write(OutputStream out, IImplementationModel model) {
		
		writeHeader(out, model);
		for (ImplementationModelPartition part : model.getPartitions()) {
			writePartition(out, part);
		}
		writeFooter(out, model);
	}

	
	public static void writePartition(OutputStream out, ImplementationModelPartition partition) {
		
		ImplementationModelFactory<?,?,?,?> factory = 
				ImplementationModelFactoryRegistry.instance().getFactory(partition.getBuilderType());
	
		try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
			if (factory != null) {
				writer.write(partition.getBuilderType().getName() + "\n");
				for(IXReference<? extends IImplementationModelElement<?>,?> ref : partition.getReferences()) {
					writer.write(factory.serializeInternalReference(ref) + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	public static void writeHeader(OutputStream out, IImplementationModel model) {
		
	}
	
	public static void writeFooter(OutputStream out, IImplementationModel model) {
		
	}
}
