package se.kau.cs.jittac.eclipse.ui.policies;

import java.util.ArrayList;

import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;
import org.eclipse.gef.mvc.fx.models.HoverModel;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.policies.DeletionPolicy;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import se.kau.cs.jittac.eclipse.ui.parts.ConnectorPart;
import se.kau.cs.jittac.model.am.Connector;

public class ShowArchitectureElementContextMenuOnClickHandler extends AbstractHandler implements IOnClickHandler {

	private ContextMenu openContextMenu = null;
	
	@Override
	public void click(MouseEvent event) {
		if (openContextMenu != null) openContextMenu.hide();
		if (!event.isSecondaryButtonDown()) {
			return; // only listen to secondary buttons
		}

		
		MenuItem deleteElementItem = new MenuItem("Delete");

		
		if (getHost() instanceof ConnectorPart) {
			Connector conn = ((ConnectorPart) getHost()).getContent();
			if (!conn.isEnvisaged()) {
				deleteElementItem.setDisable(true);
			}
		}
		
		deleteElementItem.setOnAction((e) -> {

			HoverModel hover = getHost().getViewer().getAdapter(HoverModel.class);
			if (getHost() == hover.getHover()) {
				hover.clearHover();
			}

			// query DeletionPolicy for the removal of the host part
			IRootPart<? extends Node> root = getHost().getRoot();
			DeletionPolicy delPolicy = root.getAdapter(DeletionPolicy.class);
			init(delPolicy);

			// delete all anchored connection parts
			for (IVisualPart<? extends Node> a : new ArrayList<>(getHost().getAnchoredsUnmodifiable())) {
				if (a instanceof ConnectorPart) {
					delPolicy.delete((IContentPart<? extends Node>) a);
				}
			}

			if (getHost() instanceof ConnectorPart) {
				Connector conn = ((ConnectorPart) getHost()).getContent();
				
				if (conn.getState() == Connector.STATE.ABSENCE) {
					delPolicy.delete((IContentPart<? extends Node>) getHost());
				}
				else {
					conn.getModel().makeConnectorUnenvisaged(conn);
				}
	
			}
			else {
				// delete the node part
				delPolicy.delete((IContentPart<? extends Node>) getHost());
			}
			
			
			commit(delPolicy);
		});

		
		ContextMenu contextMenu = new ContextMenu(deleteElementItem);
		contextMenu.show((Node) event.getTarget(), event.getScreenX(), event.getScreenY());
		openContextMenu = contextMenu;
	}

}
