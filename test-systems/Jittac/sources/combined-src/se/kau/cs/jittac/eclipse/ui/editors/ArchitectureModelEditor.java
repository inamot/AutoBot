package se.kau.cs.jittac.eclipse.ui.editors;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;  
import javafx.event.EventHandler;

import se.kau.cs.jittac.eclipse.ModelManager;
import se.kau.cs.jittac.eclipse.ui.ArchitectureModelModule;
import se.kau.cs.jittac.eclipse.ui.models.ItemCreationModel;
import se.kau.cs.jittac.eclipse.ui.views.BrowserView;
import se.kau.cs.jittac.eclipse.ui.views.FeatureLocationTableView;
import se.kau.cs.jittac.eclipse.util.FeatureLocationCSV;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.io.ArchitectureModelWriter;
import se.kau.cs.jittac.model.am.test.ConsoleArchitectureModelListener;
import se.kau.cs.jittac.model.mapping.JittacResourceModelRegistry;
import se.kau.cs.jittac.model.mapping.events.AbstractArchitectureMappingChangeListener;
import se.kau.cs.jittac.model.mapping.events.IArchitectureMappingChangeListener;
import se.kau.cs.jittac.model.mapping.events.MappingAdditionEvent;
import se.kau.cs.jittac.model.mapping.events.MappingRemovalEvent;
import se.kau.cs.jittac.model.mapping.test.TestResourceFactory;

public class ArchitectureModelEditor extends AbstractFXEditor {
	
	private ArchitectureModel model;
	private Component c1, c2, c3;
	private boolean mappingChanged;
	private IArchitectureMappingChangeListener mappingListener =
			new ArchitectureModelEditorMappingListener();
	
	private class ArchitectureModelEditorMappingListener extends AbstractArchitectureMappingChangeListener {
		protected void onMappingAdded(MappingAdditionEvent event) {
			mappingChanged = true;
		}

		protected void onMappingRemoved(MappingRemovalEvent event) {
			mappingChanged = true;
		}
	}
	
	public ArchitectureModelEditor() {
		super(Guice.createInjector(
				Modules.override(new ArchitectureModelModule()).
				with(new MvcFxUiModule())));
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			ArchitectureModel model = (ArchitectureModel) getContentViewer().getContents().get(0);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ArchitectureModelWriter.write(output, model);
			
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			
			file.setContents(new ByteArrayInputStream(output.toByteArray()), true, false, monitor);
			this.markNonDirty();
			output.close();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public boolean isDirty() {
		return super.isDirty() || mappingChanged;
	}
	
	@Override
	public void markNonDirty() {
		mappingChanged = false;
		super.markNonDirty();
	}
	
	public ArchitectureModel getModel() {
		return (ArchitectureModel) getContentViewer().getContents().get(0);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		IFile archModelFile = ((IFileEditorInput) input).getFile();
		ArchitectureModel model = ModelManager.instance().getArchitectureModel( archModelFile );
		
		if (model != null) {
			model.getMapping().registerListener(mappingListener);
			mappingChanged = false;
			IViewer viewer = getContentViewer();
			viewer.getContents().setAll(model);
			viewer.getRootPart().refreshVisual();
			this.setPartName( archModelFile.getName() );
		}
		else {
			throw new PartInitException("Jittac Architecture Editor could not load " + archModelFile.getName());
		}
	}
	
	@Override
	protected void hookViewers() {

		final IViewer contentViewer = getContentViewer();

		// creating parent pane for Canvas and button pane
		BorderPane pane = new BorderPane();

		//pane.setTop();
		pane.setCenter(contentViewer.getCanvas());
		pane.setRight(createToolPalette());

		pane.setMinWidth(800);
		pane.setMinHeight(600);

		Scene scene = new Scene(pane);
		getCanvas().setScene(scene);
	}
	
//	private Node createTopPalette() {
//
//	}
	
	private Node createToolPalette()
	{
		ItemCreationModel creationModel = getContentViewer().getAdapter( ItemCreationModel.class );
		
		// the toggleGroup makes sure, we only select one 
		ToggleGroup toggleGroup = new ToggleGroup();
		
		ToggleButton createNode = new ToggleButton( "New Node" );
		createNode.setToggleGroup( toggleGroup );
		createNode.setStyle( "-fx-base: lightskyblue;" );
		createNode.setMaxWidth( Double.MAX_VALUE );
		createNode.setMinHeight( 50 );
		createNode.selectedProperty().addListener( ( e, oldVal, newVal ) -> {
			creationModel.setType( newVal ? ItemCreationModel.Type.Component : ItemCreationModel.Type.None );
		} );

		ToggleButton createConn = new ToggleButton( "New Connection" );
		createConn.setToggleGroup( toggleGroup );
		createConn.setMaxWidth( Double.MAX_VALUE );
		createConn.setMinHeight( 50 );
		createConn.selectedProperty().addListener( ( e, oldVal, newVal ) -> {
	        creationModel.setType( newVal ? ItemCreationModel.Type.Connector : ItemCreationModel.Type.None );
		});
		
		// now listen to changes in the model, and deactivate buttons, if necessary
		creationModel.getTypeProperty().addListener( ( e, oldVal, newVal ) -> {
	            
			if( ItemCreationModel.Type.None == newVal )
			{
                // unselect the toggle button
                Toggle selectedToggle = toggleGroup.getSelectedToggle();
                
                if( selectedToggle != null )
                {
                    selectedToggle.setSelected( false );
                }
	        }
	    } );
		
		Button loadFeatLocs = new Button( "Load Feature Locations" );
		loadFeatLocs.setMaxWidth( Double.MAX_VALUE );
		loadFeatLocs.setMinHeight( 50 );
		loadFeatLocs.setStyle( "-fx-base: lightgreen;" );
		loadFeatLocs.setOnAction( new EventHandler<ActionEvent>() {  
			@Override  
			public void handle( ActionEvent arg0 )
			{  
				FileChooser file = new FileChooser();  
		        file.setTitle( "Open File" );  
		        File csvPath = file.showOpenDialog( null );
		        FeatureLocationCSV.load( csvPath );
		        FeatureLocationTableView.refresh();
			} 
		} );

		Button openBrowser = new Button( "View Feature Dependencies" );
		openBrowser.setMaxWidth( Double.MAX_VALUE );
		openBrowser.setMinHeight( 50 );
		openBrowser.setStyle( "-fx-base: lightgreen;" );
		openBrowser.setOnAction( new EventHandler<ActionEvent>()
		{  
			@Override  
			public void handle( ActionEvent arg0 )
			{   
				BrowserView.openBrowser();
			} 
		} );
		
		return new VBox( 20, createNode, createConn, loadFeatLocs, openBrowser );
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
        
 /*       int i = 0;
        for (IVisualPart p : viewer.getRootPart().getChildrenUnmodifiable().get(0).getChildrenUnmodifiable()) {
        	if (p instanceof ComponentPart) {
        		p.getVisual().setLayoutX(positions[i++]);
        		p.getVisual().setLayoutY(positions[i++]);
        	}
        }*/
        viewer.getRootPart().refreshVisual();
    }
	
}