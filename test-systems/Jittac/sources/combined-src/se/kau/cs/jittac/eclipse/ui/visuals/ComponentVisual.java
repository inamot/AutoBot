package se.kau.cs.jittac.eclipse.ui.visuals;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.RoundedRectangle;
import org.eclipse.gef.mvc.fx.models.SelectionModel;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class ComponentVisual extends Region {
	private static final double HORIZONTAL_PADDING = 5d;
	private static final double VERTICAL_PADDING = 5d;
	private static final double VERTICAL_SPACING = 5d;
	
	private GeometryNode<RoundedRectangle> shape;
	private VBox labelVBox;
	private TextField nameText;
	private TextFlow nameFlow;
	
	public ComponentVisual() {
        shape = new GeometryNode<>(new RoundedRectangle(0, 0, 70, 30, 8, 8));
        shape.setFill(Color.LIGHTSKYBLUE);
        shape.setStroke(Color.BLACK);
        
        nameText = new TextField();
        nameText.setEditable(false);
        nameText.setAlignment(Pos.TOP_CENTER);
        nameText.setStyle(
        		"-fx-background-insets: 0;" + 
        		"    -fx-background-color: transparent, white, transparent, white;" + 
        		"    -fx-background-radius: 0, 0, 0, 0;" + 
        		"    -fx-box-border: none;" + 
        		"    -fx-focus-color: -fx-control-inner-background;" + 
        		"    -fx-faint-focus-color: -fx-control-inner-background;" + 
        		"    -fx-text-box-border: -fx-control-inner-background;" + 
        		"    -fx-border-width: -1");
        nameText.setBlendMode(BlendMode.MULTIPLY);
        nameText.setCursor(Cursor.DEFAULT);
        nameText.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        nameText.setOnMouseClicked((event) ->
        {
        	if (event.getClickCount() >= 2)
        		nameText.setEditable(true);
        });
                         
        labelVBox = new VBox(VERTICAL_SPACING);
        labelVBox.setPadding(new Insets(VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING));
        
        labelVBox.getChildren().addAll(/*new Text("Hallo"),*/ nameText);

        // ensure shape and labels are resized to fit this visual
        shape.prefWidthProperty().bind(widthProperty());
        shape.prefHeightProperty().bind(heightProperty());
        labelVBox.prefWidthProperty().bind(widthProperty());
        labelVBox.prefHeightProperty().bind(heightProperty());
        
 
        

        
        
        //nameText.setTextOrigin(VPos.CENTER);
        
        //nameFlow = new TextFlow(nameText);
        //nameFlow.maxWidthProperty().bind(shape.widthProperty().subtract(HORIZONTAL_PADDING * 2));
        
 
        
        setMinSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        getChildren().addAll(new Group(shape), new Group(labelVBox));
	}

    @Override
    public double computeMinHeight(double width) {
        
        return labelVBox.minHeight(width);
    }

    @Override
    public double computeMinWidth(double height) {
        // ensure title is always visible
    	Text temp = new Text(nameText.getText());
    	temp.setFont(nameText.getFont());
    	double result = temp.getLayoutBounds().getWidth();
    	return Math.max(result + 20 + HORIZONTAL_PADDING * 2, 50);
    }

    @Override
    protected double computePrefHeight(double width) {
        return minHeight(width);
    }

    @Override
    protected double computePrefWidth(double height) {
        return minWidth(height);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }
	
    public TextField getNameText() {
        return nameText;
    }

    public GeometryNode<?> getGeometryNode() {
        return shape;
    }

    public void setName(String name) {
        nameText.setText(name);
        nameText.commitValue();
    }
    
    
}
