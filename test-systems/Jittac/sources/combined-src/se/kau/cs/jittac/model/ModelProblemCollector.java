package se.kau.cs.jittac.model;

import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesAddedEvent;
import se.kau.cs.jittac.model.am.events.IArchitectureModelVisitor;

public class ModelProblemCollector implements IArchitectureModelVisitor {

	private ModelProblemManager problemManager;

	public ModelProblemCollector(ModelProblemManager problemManager) {
		this.problemManager = problemManager;
	}

	@Override
	public void visit(ArchitectureModel model) {
		for (Connector connector : model.getConnectors()) {
			ConnectorReferencesAddedEvent event = new ConnectorReferencesAddedEvent(connector,
					connector.getContributingReferences(), model);
			problemManager.onConnectorReferencesAdded(event);
		}

	}

}
