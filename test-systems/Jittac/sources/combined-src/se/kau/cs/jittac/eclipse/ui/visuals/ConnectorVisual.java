package se.kau.cs.jittac.eclipse.ui.visuals;

import java.util.List;

import org.eclipse.gef.fx.nodes.Connection;
import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.fx.utils.NodeUtils;
import org.eclipse.gef.geometry.planar.Line;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.geometry.planar.Polyline;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class ConnectorVisual extends Group {
	
	private static final Double[] DIVERGENCE_DASH_STYLE = new Double[] {10.0, 6.0};
	private static final Double[] ABSENCE_DASH_STYLE = new Double[] {1.0, 5.0};
	
	private Connection connection = new Connection();
	private Text label;

    public static class ArrowHead extends Polygon {
    	public static double length = 12.0;
    	public static double width = 8.0;
        public ArrowHead() {
            super(0, 0, length, 0.5*width, length, -0.5*width);
        }

    }
    
    public ConnectorVisual() {
        ArrowHead endDecoration = new ArrowHead();
        endDecoration.setFill(Color.BLACK);
        connection.setEndDecoration(endDecoration);
        connection.addControlPoint(0, computeControlPoint());
        ((GeometryNode<?>) connection.getCurve()).setStrokeWidth(1.25);
        ((GeometryNode<?>) connection.getCurve()).setClickableAreaWidth(10.0);
        
        label = new Text("Hallo");
        Point labelPos = computeLabelPosition();
        label.setTextOrigin(VPos.CENTER);
        
        label.setLayoutX(labelPos.x());
        label.setLayoutY(labelPos.y());

        this.getChildren().addAll(connection, label);
    }
 
    @Override
    public void layoutChildren() {
    	super.layoutChildren();
        Point controlPoint = computeControlPoint();
        connection.setControlPoint(0, controlPoint);
        Point labelPos = computeLabelPosition();
        label.setLayoutX(labelPos.x());
        label.setLayoutY(labelPos.y()); 
     }
    
    
    private Point computeLabelPosition() {
    	//get middle segment
    	//int labelDistanceFactor = 5;
    	double labelWidth = label.getLayoutBounds().getWidth();
    	double labelHeight = label.getLayoutBounds().getHeight();
    	double labelDistanceFactor = 0.5 *  Math.sqrt(labelWidth*labelWidth + labelHeight*labelHeight);
    	Point p1 = connection.getStartPoint();
    	Point p2 = connection.getControlPoint(0);
    	double dx = p2.x - p1.x;
    	double dy = p2.y - p1.y;
    	
    	if (dy == 0) dy = 0.0001;
    	double ox = dy / (Math.sqrt(dx*dx + dy*dy));
    	double oy = -dx * ox / dy;
    	
    	double labelCenterX = (p1.x + p2.x) / 2 + labelDistanceFactor * ox;
    	double labelCenterY = (p1.y + p2.y) / 2 + labelDistanceFactor * oy;
 
    	return new Point(labelCenterX, labelCenterY);

    }
    
    private Point computeControlPoint() {
    	int controlPointDistanceFactor = 20;
    	Point p1 = connection.getStartPoint();
    	Point p2 = connection.getEndPoint();
    	
    	double dx = p2.x - p1.x;
    	double dy = p2.y - p1.y;
    	if (dy == 0) dy = 0.01;
    	double ox = dy / (Math.sqrt(dx*dx + dy*dy));
    	double oy = -dx * ox / dy;
    	
    	Point midPoint = this.getMidPoint();
    	
      	return new Point(midPoint.x + controlPointDistanceFactor * ox, 
      			midPoint.y + controlPointDistanceFactor * oy);
 
    }
    
    private Point getMidPoint() {
    	Point p1 = connection.getStartPoint();
    	Point p2 = connection.getEndPoint();

    	return new Point((p1.x + p2.x) / 2, (p1.y + p2.y)/ 2);
    	
    }
    
    public String getLabel() {
    	return label.getText();
    }
    
    public void setStrokeStyle(int nrOfDeps, boolean intended) {
    	if (nrOfDeps > 0) {
    		if (intended) {
    			((GeometryNode<?>) connection.getCurve()).getStrokeDashArray().clear();
    			
    		}
    		else {
    			((GeometryNode<?>) connection.getCurve()).getStrokeDashArray().setAll(DIVERGENCE_DASH_STYLE);
    		}
    	}
    	else if (intended)  {
    		((GeometryNode<?>) connection.getCurve()).getStrokeDashArray().setAll(ABSENCE_DASH_STYLE);
    	}
    }
    
    public void setLabel(String newText) {
    	label.setText(newText);
    }
    
    public Connection getConnection() {
    	return connection;
    }
  
    private Point getDecorationConnectionPoint() {
    	Point endPoint = connection.getEndPoint();
    	Point lastControlPoint = connection.getControlPoint(connection.getControlPoints().size() - 1);
    	double dx = endPoint.x - lastControlPoint.x;
    	double dy = endPoint.y - lastControlPoint.y;
    	double length = Math.sqrt(dx*dx + dy*dy); 
    	
    	double arrowX = ConnectorVisual.ArrowHead.length * dx / length;
    	double arrowY = ConnectorVisual.ArrowHead.length * dy / length;
    	
    	return new Point(endPoint.x - arrowX, endPoint.y - arrowY); 
    }

    public Polyline getOutline() {
    	
    	
    	List<Double> ahPoints = ((ArrowHead) connection.getEndDecoration()).getPoints();
    	Point p1 = new Point (ahPoints.get(2), ahPoints.get(3));
    	Point p2 = new Point (ahPoints.get(0), ahPoints.get(1));
    	Point p3 = new Point (ahPoints.get(4), ahPoints.get(5));
    	Polyline ahOutline = new Polyline(p1, p2, p3);
    	ahOutline = (Polyline)
    			NodeUtils.localToParent(connection.getEndDecoration(),
    					ahOutline);
    	
    	Point[] arrowHeadPoints = ahOutline.getPoints();

    	
    	Point newEndPoint = getDecorationConnectionPoint();
    	int nrOfControlPoints = connection.getControlPoints().size();
    	Line[] outlineElements = new Line[nrOfControlPoints + 5];
    	
    	outlineElements[0] = new Line(connection.getStartPoint(), connection.getControlPoint(0));
    	for (int i = 1; i < nrOfControlPoints; i++) {
    		outlineElements[i] = new Line(connection.getControlPoint(i - 1), connection.getControlPoint(i));
    	}
    	outlineElements[nrOfControlPoints] =
    			new Line(connection.getControlPoint(nrOfControlPoints - 1),
    					newEndPoint);

    	outlineElements[nrOfControlPoints + 1] =
    			new Line(newEndPoint, arrowHeadPoints[0]);
    	
    	outlineElements[nrOfControlPoints + 2] =
    			new Line(arrowHeadPoints[0], arrowHeadPoints[1]);

    	outlineElements[nrOfControlPoints + 3] =
    			new Line(arrowHeadPoints[1], arrowHeadPoints[2]);
    	
    	outlineElements[nrOfControlPoints + 4] =
    			new Line(arrowHeadPoints[2], newEndPoint);
    	
    	return new Polyline(outlineElements);
    }
    
}
