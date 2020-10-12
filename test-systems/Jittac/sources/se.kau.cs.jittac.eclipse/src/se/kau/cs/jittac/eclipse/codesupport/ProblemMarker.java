package se.kau.cs.jittac.eclipse.codesupport;

import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import se.kau.cs.jittac.eclipse.builders.jdt.JDTJavaReference;
import se.kau.cs.jittac.eclipse.builders.jdt.JDTJavaReferenceCodeInformation;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.am.events.AbstractArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesAddedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesRemovedEvent;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResourceFactory;

public class ProblemMarker extends AbstractArchitectureModelChangeListener {
		
	public static final String MARKER_TYPE = "se.kau.cs.jittac.eclipse.codesupport.ProblemMarker";
	public static final int SEVERITY = Marker.SEVERITY_WARNING;
	public static final String CONNECTOR_ID_ATTRIBUTE = "CONNECTOR_ID";
	
	public static final ProblemMarker instance = new ProblemMarker();
	
	public static ProblemMarker getInstance() {
		return instance;
	}

	public void onConnectorReferencesAdded(ConnectorReferencesAddedEvent event) {
		
		Connector conn = event.getModifiedConnector(); 
		if (conn.isEnvisaged()) return;
		try {
			for(IXReference<?,?> ref : event.getAddedReferences()) {
				IJittacResource jResource = ref.getResource();
				if (jResource.getResourceModelName()
						.equals(EclipseJittacResourceFactory.RESOURCE_MODEL_NAME)) {
					IResource resource = ((EclipseJittacResource) jResource).getWrappedResource();
					if (ref instanceof JDTJavaReference) {
						JDTJavaReferenceCodeInformation refInf =
							((JDTJavaReference) ref).getInternalReference();
						IMarker marker = resource.createMarker(MARKER_TYPE);
						marker.setAttribute(IMarker.SEVERITY, SEVERITY);
						marker.setAttribute(IMarker.MESSAGE, 
								ref.getTarget() + " should not be accessed in this context " +
								" (" + event.getModifiedConnector().getName() + ")");
						marker.setAttribute(IMarker.CHAR_START, refInf.offset);
						marker.setAttribute(IMarker.CHAR_END, refInf.offset + refInf.length);
						marker.setAttribute(IMarker.LINE_NUMBER, refInf.line);
						marker.setAttribute(CONNECTOR_ID_ATTRIBUTE, event.getModifiedConnector().getName());

					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public void onConnectorReferencesRemoved(ConnectorReferencesRemovedEvent event) {
		
	}
}
