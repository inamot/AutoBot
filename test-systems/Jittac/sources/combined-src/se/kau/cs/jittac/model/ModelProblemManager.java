package se.kau.cs.jittac.model;

import java.util.ArrayList;
import java.util.List;

import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.am.events.AbstractArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesAddedEvent;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.mapping.IJittacProject;

public class ModelProblemManager extends AbstractArchitectureModelChangeListener {
	
	private List<Connector> connector;
	
	public ModelProblemManager(IJittacProject project) {
		if(project == null) {
			throw new IllegalArgumentException("project must not be null");
		}
		
		connector = new ArrayList<Connector>();
		new ModelProblemCollector(this).visit(project.getArchitectureModel());
		serializeViolations();
	}

	@Override
	public void onConnectorReferencesAdded(ConnectorReferencesAddedEvent event) {
		connector.add(event.getModifiedConnector());
	}
	
	private void serializeViolations() {
		for(Connector connector: connector) {
			for(IXReference reference: connector.getContributingReferences()) {
				// do something
			}
		}
	}
	
}
