package se.kau.cs.jittac.eclipse.ui.policies;

import org.eclipse.gef.mvc.fx.behaviors.AbstractBehavior;
import org.eclipse.gef.mvc.fx.handlers.IOnStrokeHandler;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import se.kau.cs.jittac.eclipse.ui.models.ItemCreationModel;

public class AbortConnectorCreationStrokeHandler extends AbstractBehavior implements IOnStrokeHandler {

	@Override
	public void abortPress() {

	}

	@Override
	public void finalRelease(KeyEvent event) {
	}

	@Override
	public void initialPress(KeyEvent event) {

		if (!(event.getCode() == KeyCode.ESCAPE ||
				event.getCode() == KeyCode.DELETE)) 
			return;

		ItemCreationModel model =
				getHost().getRoot().
					getViewer().getAdapter(ItemCreationModel.class);
		if (model.getType() != ItemCreationModel.Type.Connector) 
			return;
		if (model.getSource() == null)
			return;
		model.setSource(null);
		model.setType(ItemCreationModel.Type.None);
	}

	@Override
	public void press(KeyEvent event) {
	}

	@Override
	public void release(KeyEvent event) {
	}

}
