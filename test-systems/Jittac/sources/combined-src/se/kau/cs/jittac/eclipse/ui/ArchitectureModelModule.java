package se.kau.cs.jittac.eclipse.ui;

import com.google.inject.multibindings.MapBinder;

import se.kau.cs.jittac.eclipse.ui.models.ItemCreationModel;
import se.kau.cs.jittac.eclipse.ui.parts.ArchitectureModelAnchorProvider;
import se.kau.cs.jittac.eclipse.ui.parts.ArchitectureModelPart;
import se.kau.cs.jittac.eclipse.ui.parts.ArchitectureModelPartsFactory;
import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;
import se.kau.cs.jittac.eclipse.ui.parts.ConnectorGeometricOutlineProvider;
import se.kau.cs.jittac.eclipse.ui.parts.ConnectorPart;
import se.kau.cs.jittac.eclipse.ui.parts.ConnectorSelectionFeedbackPart;
import se.kau.cs.jittac.eclipse.ui.parts.CreateConnectorFeedbackPartBehavior;
import se.kau.cs.jittac.eclipse.ui.parts.CreateConnectorFeedbackPartFactory;
import se.kau.cs.jittac.eclipse.ui.parts.SelectionColorProvider;
import se.kau.cs.jittac.eclipse.ui.policies.AbortConnectorCreationStrokeHandler;
import se.kau.cs.jittac.eclipse.ui.policies.ComponentOnDragHandler;
import se.kau.cs.jittac.eclipse.ui.policies.CreateComponentOnClickHandler;
import se.kau.cs.jittac.eclipse.ui.policies.CreateConnectorOnClickHandler;
import se.kau.cs.jittac.eclipse.ui.policies.ShowArchitectureElementContextMenuOnClickHandler;
import se.kau.cs.jittac.eclipse.ui.policies.ShowArchitectureModelContextMenuOnClickHandler;
import se.kau.cs.jittac.eclipse.ui.policies.TestOnDragHandler;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.common.adapt.inject.AdapterMaps;
import org.eclipse.gef.mvc.fx.MvcFxModule;
import org.eclipse.gef.mvc.fx.handlers.FocusAndSelectOnClickHandler;
import org.eclipse.gef.mvc.fx.handlers.TranslateSelectedOnDragHandler;
import org.eclipse.gef.mvc.fx.parts.DefaultHoverFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.DefaultSelectionFeedbackPartFactory;
import org.eclipse.gef.mvc.fx.parts.SelectionFeedbackPart;
import org.eclipse.gef.mvc.fx.policies.TransformPolicy;
import org.eclipse.gef.mvc.fx.providers.ShapeOutlineProvider;

public class ArchitectureModelModule extends MvcFxModule {

    @Override
    protected void bindIContentPartFactoryAsContentViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
        // bind MindMapPartsFactory adapter to the content viewer
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ArchitectureModelPartsFactory.class);
    }

    /**
     *
     * @param adapterMapBinder
     */
    @SuppressWarnings("restriction")
	protected void bindComponentPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
        // bind anchor provider used to create the connection anchors
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ArchitectureModelAnchorProvider.class);

        // bind a geometry provider, which is used in our anchor provider
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ShapeOutlineProvider.class);
        
        // provides a hover feedback to the shape, used by the HoverBehavior
        AdapterKey<?> role = AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER);
        adapterMapBinder.addBinding(role).to(ShapeOutlineProvider.class);

		// provides a selection feedback to the shape
		role = AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER);
		adapterMapBinder.addBinding(role).to(ShapeOutlineProvider.class);
        
         // support moving nodes via mouse drag
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TransformPolicy.class);
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ComponentOnDragHandler.class);
        
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateConnectorOnClickHandler.class);
        
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ShowArchitectureElementContextMenuOnClickHandler.class);
    }
    
    @SuppressWarnings("restriction")
	protected void bindConnectorPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
        // provides a hover feedback to the shape, used by the HoverBehavior
        AdapterKey<?> role = AdapterKey.role(DefaultHoverFeedbackPartFactory.HOVER_FEEDBACK_GEOMETRY_PROVIDER);
        adapterMapBinder.addBinding(role).to(ConnectorGeometricOutlineProvider.class);

		// provides a selection feedback to the shape
		role = AdapterKey.role(DefaultSelectionFeedbackPartFactory.SELECTION_FEEDBACK_GEOMETRY_PROVIDER);
		adapterMapBinder.addBinding(role).to(ConnectorGeometricOutlineProvider.class);
	
		adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ShowArchitectureElementContextMenuOnClickHandler.class);
		//AdapterKey<?> key = AdapterKey.get(SelectionFeedbackPart.class); 
		//adapterMapBinder.addBinding(key).to(ConnectorSelectionFeedbackPart.class);
    }
    
    @SuppressWarnings("restriction")
    protected void bindArchitectureModelPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
        
    	// add the focus and select policy to every part, listening to clicks
        // and changing the focus and selection model
        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(FocusAndSelectOnClickHandler.class);
        
        
        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TestOnDragHandler.class);
        
        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(AbortConnectorCreationStrokeHandler.class);
        
        
    }

//    @SuppressWarnings("restriction")
//    @Override
//	protected void bindHoverFeedbackPartFactoryAsContentViewerAdapter(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
//    	adapterMapBinder
//			.addBinding(AdapterKey.role(HoverBehavior.HOVER_FEEDBACK_PART_FACTORY))
//			.to(HoverFeedbackPartFactory.class);
//    }
    
    @SuppressWarnings("restriction")
	@Override
    protected void bindAbstractContentPartAdapters(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
        super.bindAbstractContentPartAdapters(adapterMapBinder);

        // binding the HoverOnHoverPolicy to every part
        // if a mouse is moving above a part it is set i the HoverModel
        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverOnHoverHandler.class);

        // add the focus and select policy to every part, listening to clicks
        // and changing the focus and selection model
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(FocusAndSelectOnClickHandler.class);

        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(TestOnDragHandler.class);
        
        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(AbortConnectorCreationStrokeHandler.class);
        
        
    }
    
    @SuppressWarnings("restriction")
	@Override
    protected void bindIRootPartAdaptersForContentViewer(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
        super.bindIRootPartAdaptersForContentViewer(adapterMapBinder);

        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateComponentOnClickHandler.class);
        // binding a Hover Behavior to the root part. it will react to
        // HoverModel changes and render the hover part
        //adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(HoverBehavior.class);
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(CreateConnectorFeedbackPartBehavior.class);
        
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(AbortConnectorCreationStrokeHandler.class);
        
        adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ShowArchitectureModelContextMenuOnClickHandler.class);
        
    }
    
    @SuppressWarnings("restriction")
	@Override
    protected void bindIViewerAdaptersForContentViewer(MapBinder<AdapterKey<?>, Object> adapterMapBinder) {
    	super.bindIViewerAdaptersForContentViewer(adapterMapBinder);
    	// bind the model to the content viewer
    	adapterMapBinder.addBinding(AdapterKey.defaultRole()).to(ItemCreationModel.class);
    	
        AdapterKey<?> role = AdapterKey.role(DefaultSelectionFeedbackPartFactory.PRIMARY_SELECTION_FEEDBACK_COLOR_PROVIDER);
        adapterMapBinder.addBinding(role).to(SelectionColorProvider.class);
        
        role = AdapterKey.role(DefaultSelectionFeedbackPartFactory.SECONDARY_SELECTION_FEEDBACK_COLOR_PROVIDER);
        adapterMapBinder.addBinding(role).to(SelectionColorProvider.class);
        
        role = AdapterKey.role(CreateConnectorFeedbackPartBehavior.CREATE_FEEDBACK_PART_FACTORY);
        adapterMapBinder.addBinding(role).to(CreateConnectorFeedbackPartFactory.class);
        
        
    }
    
    @SuppressWarnings("restriction")
	@Override
    protected void configure() {
        // start the default configuration
        super.configure();

        bindComponentPartAdapters(AdapterMaps.getAdapterMapBinder(binder(), ComponentPart.class));
        bindConnectorPartAdapters(AdapterMaps.getAdapterMapBinder(binder(), ConnectorPart.class));
        bindArchitectureModelPartAdapters(AdapterMaps.getAdapterMapBinder(binder(), ArchitectureModelPart.class));
        
        bind(SelectionFeedbackPart.class).to(ConnectorSelectionFeedbackPart.class);
    }
}
