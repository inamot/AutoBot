package se.kau.cs.jittac.eclipse.ui.policies;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;
import org.eclipse.gef.mvc.fx.operations.ChangeSelectionOperation;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.reflect.TypeToken;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import se.kau.cs.jittac.eclipse.ui.models.ItemCreationModel;
import se.kau.cs.jittac.eclipse.ui.operations.MarkConnectorAsEnvisagedOperation;
import se.kau.cs.jittac.eclipse.ui.parts.ArchitectureModelPart;
import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;
import se.kau.cs.jittac.model.am.Connector;

public class CreateConnectorOnClickHandler extends AbstractHandler implements IOnClickHandler {

	@Override
	public void click(MouseEvent e) {
		if (!e.isPrimaryButtonDown()) {
			return; 
		}
		
		IViewer viewer = getHost().getRoot().getViewer();
		ItemCreationModel creationModel = viewer.getAdapter(ItemCreationModel.class);
		if (creationModel.getType() != ItemCreationModel.Type.Connector) {
			return; // don't want to create a connection
		}
		
		if (creationModel.getSource()==null) {
			// the host is the source
			creationModel.setSource((ComponentPart) getHost());
			return; // wait for the next click
		}
		
		// okay, we have a pair
		ComponentPart source = creationModel.getSource();
		ComponentPart target = (ComponentPart) getHost();

        // check if valid
        if (source == target) {
                return;
        }
  
		IRootPart<? extends Node> root = getHost().getRoot();
		// get the policy bound to the IRootPart
		@SuppressWarnings("serial")
		CreationPolicy creationPolicy = root.getAdapter(new TypeToken<CreationPolicy>() {});
		// initialize the policy
		init(creationPolicy);
        
        IVisualPart<? extends Node> part = getHost().getRoot().getChildrenUnmodifiable().get(0);
		if (part instanceof ArchitectureModelPart) {
			ArchitectureModelPart amPart = (ArchitectureModelPart) part;
			Connector conn = amPart.getContent().getConnectorByComponents(source.getContent(), target.getContent());
			if (conn != null) {
				if (!conn.isEnvisaged()) {
					ITransactionalOperation op = new MarkConnectorAsEnvisagedOperation(conn);
					conn.getModel().changeConnectorState(conn, true);
					try {
						viewer.getDomain().execute(op, null);
					} catch (ExecutionException exc) {
						exc.printStackTrace();
					}
				}
			}
			else {
				conn = Connector.createConnector(source.getContent(), target.getContent());
				// create a IContentPart for our new model. We don't use the
				// returned content-part
				creationPolicy.create(conn, (ArchitectureModelPart) part,
						HashMultimap.<IContentPart<? extends Node>, String>create());
				// execute the creation

			}
			commit(creationPolicy);
            try {
                    viewer.getDomain().execute(new ChangeSelectionOperation(viewer, Collections.singletonList(target)),
                                    null);
            } catch (ExecutionException e1) {
            }
            amPart.refreshContentChildren();
    		// finally reset creationModel
    		creationModel.setSource(null);
    		creationModel.setType(ItemCreationModel.Type.None);
		}
	}
}
