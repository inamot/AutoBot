package se.kau.cs.jittac.eclipse.ui.policies;

import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.common.collect.HashMultimap;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import se.kau.cs.jittac.eclipse.ui.models.ItemCreationModel;
import se.kau.cs.jittac.eclipse.ui.parts.ArchitectureModelPart;
import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;
import se.kau.cs.jittac.model.am.Component;

public class CreateComponentOnClickHandler extends AbstractHandler implements IOnClickHandler {

	@Override
	public void click(MouseEvent e) {
		if (!e.isPrimaryButtonDown()) {
			return; // wrong mouse button
		}

		IViewer viewer = getHost().getRoot().getViewer();
		ItemCreationModel creationModel = viewer.getAdapter(ItemCreationModel.class);
		if (creationModel == null) {
			throw new IllegalStateException("No ItemCreationModel bound to viewer!");
		}

		if (creationModel.getType() != ItemCreationModel.Type.Component) {
			// don't want to create a node
			return;
		}
		IVisualPart<? extends Node> part = viewer.getRootPart().getChildrenUnmodifiable().get(0);

		if (part instanceof ArchitectureModelPart) {
			ArchitectureModelPart  amp = (ArchitectureModelPart) part;
			// calculate the mouse coordinates
			// determine coordinates of new nodes origin in model coordinates
			Point2D mouseInLocal = part.getVisual().sceneToLocal(e.getSceneX(), e.getSceneY());

			Component comp = Component.createComponent(amp.getContent());

			// GEF provides the CreatePolicy and operations to add a new element
			// to the model
			IRootPart<? extends Node> root = getHost().getRoot();
			// get the policy bound to the IRootPart
			CreationPolicy creationPolicy = root.getAdapter(CreationPolicy.class);
			// initialize the policy
			init(creationPolicy);
			// create a IContentPart for our new model. We don't use the
			// returned content-part

			//creationPolicy.create(comp, amp,
			//		HashMultimap.<IContentPart<? extends Node>, String>create());
			
			IContentPart<?> newPart = creationPolicy.create(comp, amp,
					amp.getContent().getComponents().size(),
					HashMultimap.<IContentPart<? extends Node>, String>create(),
					true, true); 
			// execute the creation
			commit(creationPolicy);
			((ComponentPart) newPart).getVisual().setLayoutX(mouseInLocal.getX());
			((ComponentPart) newPart).getVisual().setLayoutY(mouseInLocal.getY());
		}
		// clear the creation selection
		creationModel.setType(ItemCreationModel.Type.None);


	}

}
