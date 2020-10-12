package se.kau.cs.jittac.model.am.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IJittacResourceFactory;
import se.kau.cs.jittac.model.mapping.JittacResourceModelRegistry;


/**
 * Class for reading in a serialized architecture model (serialized as XML).
 * @author Sebastian Herold
 *
 */
public class ArchitectureModelReader {

	static class ArchitectureModelHandler extends DefaultHandler {
		private ArchitectureModel model = null;
		IJittacResourceFactory resourceFactory = null;
		
		public ArchitectureModelHandler() {
		}		
		
		public ArchitectureModelHandler(Path file) {
		}
		
		public ArchitectureModel getModel() {
			return model;
		}

		public void startElement(String uri, String name,
			      				 String qname, Attributes atts)
		throws SAXException {
			
			try {
				if (name.equals("model")) {
					model = ArchitectureModel.createArchitectureModel();
				}
				else if (name.equals("mapping")) {
					String resourceModelName = atts.getValue("resourceModel");
					resourceFactory =
							JittacResourceModelRegistry.INSTANCE.getResourceModelFor(resourceModelName);
					if (resourceFactory == null) {
						throw new IllegalArgumentException("Unknown resource model.");
					}
					
				} else if (name.equals("component")) {
					String id = atts.getValue("id");
					if (id == null)
						throw new IllegalArgumentException();
					
					model.createComponent(atts.getValue("name"), UUID.fromString(id));

				} else if (name.equals("connector")) {
					Component source = model.getComponentById(UUID.fromString(atts.getValue("source")));
					Component target = model.getComponentById(UUID.fromString(atts.getValue("target")));
					if (source == null || target == null)
						throw new IllegalArgumentException();
					Connector con = model.createConnector(source, target);
					con.setEnvisaged(true);
				} else if (name.equals("map")) {
					if (resourceFactory == null) 
						throw new NullPointerException("Mapping cannot be resolved.");
					String target = atts.getValue("target");
					String handle = atts.getValue("handle");
					String projectHandle = atts.getValue("projectHandle");
					if (target == null || handle == null || projectHandle == null)
						throw new NullPointerException("Mapping cannot be resolved.");
	
					Component comp = model.getComponentById(UUID.fromString(target));
					IJittacProject jProject = resourceFactory.createJittacProjectFromPersistentHandle(projectHandle);
					IJittacResource jResource = resourceFactory.createJittacResourceFromPersistentHandle(handle, jProject);
					
					if (jResource != null && comp != null && jProject != null) {
						model.getMapping().addMapping(comp, jResource);
					}
					else {
						throw new NullPointerException("Mapping cannot be resolved.");
					}
				} else if (name.equals("email")) {
					//String url = atts.getValue("url");
					//TODO: add this functionality
					//model.setEmail(url);
				} else if (name.equals("project")) {
					String handle = atts.getValue("handle");
//					IJittacProject proj = resourceFactory.createJittacProjectFromPersistentHandle(handle);
//					model.manageProject(proj);
				}
		
			} catch (IllegalArgumentException ex) {
				// TODO: Report errors in the file!
				ex.printStackTrace();
			}
		}
	}

	public static ArchitectureModel read(Path file) {
		ArchitectureModelHandler handler = new ArchitectureModelHandler(file);
		// Parse the XML file containing the model.
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(handler);
			parser.parse(file.toUri().toString());
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} // TODO: Handle the errors properly!
	
		finalizeModel(handler.getModel());
		return handler.getModel();
	}
	
	public static ArchitectureModel read(InputStream input) {
		ArchitectureModelHandler handler = new ArchitectureModelHandler();
		// Parse the XML file containing the model.
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(handler);
			parser.parse(new InputSource(input));
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} // TODO: Handle the errors properly!
	
		finalizeModel(handler.getModel());
		return handler.getModel();
	}	
	
	private static void finalizeModel(ArchitectureModel model) {
		
		for (IJittacProject relevantProject : model.getMapping().getManagedProjects()) {
			relevantProject.getImplementationModel().registerImplementationChangeListener(model);
			//Semi-hack: if model depends on an implementation model im that has already been loaded
			//before m was registered as a listener of im, the following line causes model to update anyway.
			model.onCompleteLoad(relevantProject.getImplementationModel());
		}
	}
}
