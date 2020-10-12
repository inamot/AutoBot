package se.kau.cs.jittac.model.am.io;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IMappableElement;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;

/**
 * Class for serializing an architecture model in XML format.
 * @author Sebastian Herold
 *
 */

public class ArchitectureModelWriter {
	private static final String EOL = System.getProperty("line.separator");
    
	
	protected static void writeIndentation(XMLStreamWriter writer, int level)
		throws XMLStreamException {
		String indent = "";
		for (int i=0; i < level; ++i)
			indent += "    ";
		writer.writeCharacters(indent);
	}
	
	
    protected static void writeComponents(XMLStreamWriter writer, ArchitectureModel model, int level)
        throws XMLStreamException {

    	// Write all the components.
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("components");
        writer.writeCharacters(EOL);

        Iterator<Component> iter = model.getComponents().iterator();
        while (iter.hasNext()) {
            Component component = iter.next();
            
            writeIndentation(writer, level);
            writer.writeEmptyElement("component");
            writer.writeAttribute("id", component.getId().toString());
            writer.writeAttribute("name", component.getName());
            writer.writeCharacters(EOL);
        }
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }
    
    protected static void writeConnectors(XMLStreamWriter writer, ArchitectureModel model, int level)
        throws XMLStreamException {
        // Write all the connectors.
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("connectors");
        writer.writeCharacters(EOL);

        Iterator<Connector> iter = model.getConnectors().iterator();
        while (iter.hasNext()) {
            Connector connector = iter.next();
            if (!connector.isEnvisaged() || connector.isReflexive())
                continue;
            
            // Write the connector.
            writeIndentation(writer, level);
            writer.writeEmptyElement("connector");
            writer.writeAttribute("source", connector.getSrc().getId().toString());
            writer.writeAttribute("target", connector.getTrg().getId().toString());
            writer.writeCharacters(EOL);
        }
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }
    
    protected static void writeMappings(XMLStreamWriter writer, ArchitectureModel model, int level)
        throws XMLStreamException {
        
    	ArchitectureMapping mapping = model.getMapping();
        
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("mapping");
        writer.writeAttribute("resourceModel", mapping.getResourceFactory().getResourceModelName());
        writer.writeCharacters(EOL);


        Set<Component> components = model.getComponents();
        for (Component comp : components) {
           Set<IMappableElement> resources = mapping.getMappedResources(comp);
           
           for (IMappableElement resource : resources) {
               writeIndentation(writer, level);
               writer.writeEmptyElement("map");
               writer.writeAttribute("target", comp.getId().toString());
               writer.writeAttribute("handle", resource.getPersistentHandle());
               writer.writeAttribute("projectHandle", resource.getProject().getPersistentHandle());
               writer.writeCharacters(EOL);
           }
        }
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    protected static void writeManagedProjects(XMLStreamWriter writer, ArchitectureModel model, int level)
    	throws XMLStreamException {
    	
    	ArchitectureMapping mapping = model.getMapping();
        
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("projects");
        writer.writeAttribute("resourceModel", mapping.getResourceFactory().getResourceModelName());
        writer.writeCharacters(EOL);
        
        for (IJittacProject project : model.getManagedProjects()) {
        	writer.writeEmptyElement("project");
        	writer.writeAttribute("handle", project.getPersistentHandle());
        	writer.writeCharacters(EOL);
        }
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }
    
    public static void write(OutputStream stream, ArchitectureModel model){
        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
            int level = 0;

            // Write header and open the root component.
            writer.writeStartDocument();
            
            writer.writeCharacters(EOL);
            writer.writeStartElement("model");
            //writer.writeCharacters(EOL);
            
            // Write all file contents.
            level++;
            writeComponents(writer, model, level);
            writeConnectors(writer, model, level);
            //writeManagedProjects(writer, model, level);
            writeMappings(writer, model, level);
            writeEmail(writer, model, level);
            level--;
            
            // Finish the document.
            writer.writeEndElement();
            //writer.writeCharacters(EOL);
            writer.writeEndDocument();
            
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
    /**
	 * @since 0.2
	 */
    protected static void writeEmail(XMLStreamWriter writer, ArchitectureModel model, int level)
            throws XMLStreamException {

        	// Write all the components.
            writer.writeCharacters(EOL);
            writeIndentation(writer, level++);
            writer.writeEmptyElement("email");
            writer.writeAttribute("url", "joe@somewhere.se");
            writeIndentation(writer, --level);
            writer.writeCharacters(EOL);
        }
}
