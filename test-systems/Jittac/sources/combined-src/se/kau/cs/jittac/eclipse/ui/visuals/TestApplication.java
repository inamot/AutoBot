package se.kau.cs.jittac.eclipse.ui.visuals;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.mvc.fx.domain.HistoricizingDomain;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Guice;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import se.kau.cs.jittac.eclipse.ui.ArchitectureModelModule;
import se.kau.cs.jittac.eclipse.ui.models.ItemCreationModel;
import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.test.ConsoleArchitectureModelListener;
import se.kau.cs.jittac.model.mapping.JittacResourceModelRegistry;
import se.kau.cs.jittac.model.mapping.test.JittacResourceStub;
import se.kau.cs.jittac.model.mapping.test.TestResourceFactory;

public class TestApplication extends Application {

	private ArchitectureModel model;
	private Component c1, c2, c3;
		
	public static void main(String[] args) {
	     Application.launch(args);
	}
	
	private Stage primaryStage;
	private HistoricizingDomain domain;
	
	    /**
	 * Returns the content viewer of the domain
	 *
	 * @return
	 */
	private IViewer getContentViewer() {
	    return domain.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
	}
	
	/**
	 * Creating JavaFX widgets and set them to the stage.
	 */
	private void hookViewers() {
		   BorderPane pane = new BorderPane();

			//pane.setTop(createButtonBar());
			pane.setCenter(getContentViewer().getCanvas());
			pane.setRight(createToolPalette());

			pane.setMinWidth(800);
			pane.setMinHeight(600);
			
			Scene scene = new Scene(pane);
			primaryStage.setScene(scene);
	}
    

	private Node createButtonBar() {
		Button undoButton = new Button("Undo");
		undoButton.setDisable(true);
		undoButton.setOnAction((e) -> {
			try {
				domain.getOperationHistory().undo(domain.getUndoContext(), null, null);
			} catch (org.eclipse.core.commands.ExecutionException e1) {
				e1.printStackTrace();
			}
		});

		Button redoButton = new Button("Redo");
		redoButton.setDisable(true);
		redoButton.setOnAction((e) -> {
			try {
				domain.getOperationHistory().redo(domain.getUndoContext(), null, null);
			} catch (org.eclipse.core.commands.ExecutionException e1) {
				e1.printStackTrace();
			}
		});

		// add listener to operation history in our domain 
		// and enable/disable buttons
		domain.getOperationHistory().addOperationHistoryListener((e) -> {
			IUndoContext ctx = domain.getUndoContext();
			undoButton.setDisable(!e.getHistory().canUndo(ctx));
			redoButton.setDisable(!e.getHistory().canRedo(ctx));
		});

		return new HBox(10, undoButton, redoButton);
	}

	private Node createToolPalette() {
		ItemCreationModel creationModel = getContentViewer().getAdapter(ItemCreationModel.class);
		
		ComponentVisual graphic = new ComponentVisual();
		graphic.setName("New Node");

		// the toggleGroup makes sure, we only select one 
		ToggleGroup toggleGroup = new ToggleGroup();
		
		ToggleButton createNode = new ToggleButton("", graphic);
		createNode.setToggleGroup(toggleGroup);
		createNode.selectedProperty().addListener((e, oldVal, newVal) -> {
	                creationModel.setType(newVal ? ItemCreationModel.Type.Component : ItemCreationModel.Type.None);
		});

		ToggleButton createConn = new ToggleButton("New Connection");
		createConn.setToggleGroup(toggleGroup);
		createConn.setMaxWidth(Double.MAX_VALUE);
		createConn.setMinHeight(50);
		createConn.selectedProperty().addListener((e, oldVal, newVal) -> {
	                creationModel.setType(newVal ? ItemCreationModel.Type.Connector : ItemCreationModel.Type.None);
		});
		
		// now listen to changes in the model, and deactivate buttons, if necessary
		creationModel.getTypeProperty().addListener((e, oldVal, newVal) -> {
	            if (ItemCreationModel.Type.None == newVal) {
	                // unselect the toggle button
	                Toggle selectedToggle = toggleGroup.getSelectedToggle();
	                if (selectedToggle != null) {
	                    selectedToggle.setSelected(false);
	                }
	            }
	        });
		
		return new VBox(20, createNode, createConn);
	}
	
	private void populateViewerContents() {
		double[] positions = new double[] {100, 100, 500, 100, 300, 400};
		JittacResourceModelRegistry.INSTANCE.registerResourceModel(TestResourceFactory.INSTANCE);
		JittacResourceModelRegistry.INSTANCE.setDefaultResourceModel(TestResourceFactory.INSTANCE);
        model = ArchitectureModel.createArchitectureModel();
		model.registerListener(new ConsoleArchitectureModelListener());
        c1 = model.createComponent("C1");
        c2 = model.createComponent("C2");
        c3 = model.createComponent("C3");
		

        IViewer viewer = getContentViewer();
        viewer.getContents().setAll(model);
        
        int i = 0;
        for (IVisualPart p : viewer.getRootPart().getChildrenUnmodifiable().get(0).getChildrenUnmodifiable()) {
        	if (p instanceof ComponentPart) {
        		p.getVisual().setLayoutX(positions[i++]);
        		p.getVisual().setLayoutY(positions[i++]);
        	}
        }
        viewer.getRootPart().refreshVisual();
    }
	
	@Override
	public void start(Stage primaryStage) throws Exception {
	       ArchitectureModelModule module = new ArchitectureModelModule();
	        this.primaryStage = primaryStage;
	        // create domain using guice
	        this.domain = (HistoricizingDomain) Guice.createInjector(module).getInstance(IDomain.class);

	        // create viewers
	        hookViewers();

	        // activate domain
	        domain.activate();

	        // load contents
	        populateViewerContents();

	        // set-up stage
	        primaryStage.setResizable(true);
	        primaryStage.setTitle("GEF Jittac Model");
	        primaryStage.sizeToScene(); 	
	        primaryStage.show();
	        
	       
	        //model.removeComponent(c3);
	        
	        Runnable r = new Runnable() {
				@Override
				public void run() {
			        try {
						model.createConnector(c1, c2);
						Thread.sleep(100);
						model.createConnector(c1, c3);
						Thread.sleep(100);
						model.getMapping().addMapping(c1, JittacResourceStub.INSTANCE_1);
						Thread.sleep(100);
						model.getMapping().addMapping(c2, JittacResourceStub.INSTANCE_2);
						Thread.sleep(100);
						model.getMapping().addMapping(c3, JittacResourceStub.INSTANCE_212);
						//model.getMapping().removeMapping(c2, JittacResourceStub.INSTANCE_2);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	        };
	        //Platform.runLater(r);
	        new Thread(r).start();
	        
	        
	  }

}


