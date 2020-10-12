package se.kau.cs.jittac.eclipse.ui.views;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.mvc.fx.models.FocusModel;
import org.eclipse.gef.mvc.fx.models.HoverModel;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.ui.actions.FitToViewportAction;
import org.eclipse.gef.mvc.fx.ui.actions.FitToViewportLockAction;
import org.eclipse.gef.mvc.fx.ui.actions.ScrollActionGroup;
import org.eclipse.gef.mvc.fx.ui.actions.ZoomActionGroup;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXView;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

import com.google.inject.Guice;
import com.google.inject.util.Modules;

import javafx.scene.Scene;
import se.kau.cs.jittac.eclipse.ui.ArchitectureModelModule;
import se.kau.cs.jittac.eclipse.ui.ArchitectureModelModuleUI;

public class ArchitectureModelView extends AbstractFXView {

	
	private ZoomActionGroup zoomActionGroup;
	private ScrollActionGroup scrollActionGroup;
	private FitToViewportLockAction fitToViewportLockAction;
	
	
	public ArchitectureModelView() {
		super(Guice.createInjector(Modules.override(new ArchitectureModelModule())
				.with(new ArchitectureModelModuleUI())));
	}

	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		// create actions
		zoomActionGroup = new ZoomActionGroup(new FitToViewportAction());
		getContentViewer().setAdapter(zoomActionGroup);
		
		fitToViewportLockAction = new FitToViewportLockAction();
		getContentViewer().setAdapter(fitToViewportLockAction);
		
		scrollActionGroup = new ScrollActionGroup();
		getContentViewer().setAdapter(scrollActionGroup);
		
		// contribute to toolbar
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager mgr = actionBars.getToolBarManager();
		zoomActionGroup.fillActionBars(actionBars);
		mgr.add(new Separator());
		mgr.add(fitToViewportLockAction);
		mgr.add(new Separator());
		scrollActionGroup.fillActionBars(actionBars);
	}
	
	@Override
	public void dispose() {
		// clear viewer models
		getContentViewer().getAdapter(SelectionModel.class).clearSelection();
		getContentViewer().getAdapter(HoverModel.class).clearHover();
		getContentViewer().getAdapter(FocusModel.class).setFocus(null);
		getContentViewer().contentsProperty().clear();
		/*getPaletteViewer().getAdapter(SelectionModel.class).clearSelection();
		getPaletteViewer().getAdapter(HoverModel.class).clearHover();
		getPaletteViewer().getAdapter(FocusModel.class).setFocus(null);
		getPaletteViewer().contentsProperty().clear();*/

		// dispose actions
		if (zoomActionGroup != null) {
			getContentViewer().unsetAdapter(zoomActionGroup);
			zoomActionGroup.dispose();
			zoomActionGroup = null;
		}
		if (scrollActionGroup != null) {
			getContentViewer().unsetAdapter(scrollActionGroup);
			scrollActionGroup.dispose();
			scrollActionGroup = null;
		}
		if (fitToViewportLockAction != null) {
			getContentViewer().unsetAdapter(fitToViewportLockAction);
			fitToViewportLockAction = null;
		}

		super.dispose();
	}
	
	@Override
	protected void hookViewers() {
		// build viewers composite
		//MvcLogoExampleViewersComposite viewersComposite = new MvcLogoExampleViewersComposite(
		//		getContentViewer(), getPaletteViewer());
		// create scene and populate canvas
		//getCanvas().setScene(new Scene(viewersComposite.getComposite()));
		getCanvas().setScene(new Scene(getContentViewer().getCanvas()));
	}
}
