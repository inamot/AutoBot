package se.kau.cs.jittac.eclipse.ui.parts;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.IGeometry;
import org.eclipse.gef.mvc.fx.parts.SelectionFeedbackPart;

import javafx.scene.shape.StrokeType;

public class ConnectorSelectionFeedbackPart extends SelectionFeedbackPart {

	public ConnectorSelectionFeedbackPart() {
		
	}
	
	@Override
	protected GeometryNode<IGeometry> doCreateVisual() {
		GeometryNode<IGeometry> feedbackVisual = super.doCreateVisual();
		feedbackVisual.setStrokeWidth(3);
		feedbackVisual.setStrokeType(StrokeType.INSIDE);
		
		return feedbackVisual;
	}
}
