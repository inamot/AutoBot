package se.kau.cs.jittac.eclipse.ui.policies;

import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnDragHandler;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class TestOnDragHandler extends AbstractHandler implements IOnDragHandler {

	@Override
	public void abortDrag() {
		// TODO Auto-generated method stub

	}

	@Override
	public void drag(MouseEvent e, Dimension delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endDrag(MouseEvent e, Dimension delta) {
		System.out.println("End of drag");

	}

	@Override
	public void hideIndicationCursor() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean showIndicationCursor(KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showIndicationCursor(MouseEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startDrag(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
