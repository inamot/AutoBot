package se.kau.cs.jittac.eclipse.ui.policies;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.mvc.fx.handlers.TranslateSelectedOnDragHandler;

import javafx.scene.input.MouseEvent;
import se.kau.cs.jittac.eclipse.Activator;
import se.kau.cs.jittac.eclipse.ModelManager;
import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;

public class ComponentOnDragHandler extends TranslateSelectedOnDragHandler {

	@Override
	public void endDrag(MouseEvent e, Dimension delta) {
		super.endDrag(e, delta);
		try {
			if (getHost() instanceof ComponentPart) {
				ComponentPart part = (ComponentPart) getHost();
				double xpos = part.getVisual().getLayoutX() + delta.getWidth();
				double ypos = part.getVisual().getLayoutY() + delta.getHeight();
				IFile file = ModelManager.instance().getFile(part.getContent().getModel());
				String id = part.getContent().getId().toString();
				file.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, id + "_x"), Double.toString(xpos));
				file.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, id + "_y"), Double.toString(ypos));
			}
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}
}
