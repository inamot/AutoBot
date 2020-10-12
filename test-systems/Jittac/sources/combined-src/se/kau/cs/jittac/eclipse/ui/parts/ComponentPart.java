package se.kau.cs.jittac.eclipse.ui.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.swt.dnd.DropTarget;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.transform.Affine;
import se.kau.cs.jittac.eclipse.Activator;
import se.kau.cs.jittac.eclipse.ModelManager;
import se.kau.cs.jittac.eclipse.ui.operations.SetComponentNameOperation;
import se.kau.cs.jittac.eclipse.ui.visuals.ComponentVisual;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;

public class ComponentPart extends AbstractContentPart<ComponentVisual>
implements ITransformableContentPart<ComponentVisual> {

	private ListChangeListener<IContentPart<? extends Node>> selectionModelObserver =
			new ListChangeListener<IContentPart<? extends Node>>() {
		@Override
		public void onChanged(ListChangeListener.Change<? extends IContentPart<? extends Node>> c) {
			boolean removed = false;
			boolean added = false;
			IViewer viewer = getRoot().getViewer();
			SelectionModel selectionModel = viewer.getAdapter(SelectionModel.class);
			boolean currentlySelected = 
					selectionModel.getSelectionUnmodifiable().contains(ComponentPart.this);
			ComponentPart.this.getVisual().getNameText().setEditable(false);
			
			while (c.next()) {
				if (c.wasRemoved()) {
					if (c.getRemoved().contains(ComponentPart.this)) {
						removed = true;
					}
				}
				else if (c.wasAdded()) {
					if (!c.getAddedSubList().contains(ComponentPart.this)) {
						added = true;
					}
				}
			}
			if (!currentlySelected && removed
				|| currentlySelected && !added) {
				String newName = ComponentPart.this.getVisual().getNameText().getText();
				String oldName = getContent().getName();
				if (!newName.equals(oldName)) {
					ITransactionalOperation op = new SetComponentNameOperation(ComponentPart.this, newName);
					try {
						getRoot().getViewer().getDomain().execute(op, null);
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				refreshVisual();
			}
		}
	};
	
	@Override
	protected void doActivate() {
		super.doActivate();
		IViewer viewer = getRoot().getViewer();
		
		SelectionModel selectionModel = viewer.getAdapter(SelectionModel.class);
		selectionModel.selectionUnmodifiableProperty().addListener(selectionModelObserver);
	}
	
	@Override
	protected void doDeactivate() {
		IViewer viewer = getRoot().getViewer();
		SelectionModel selectionModel = viewer.getAdapter(SelectionModel.class);
		selectionModel.selectionUnmodifiableProperty().removeListener(selectionModelObserver);
	}
	
	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected ComponentVisual doCreateVisual() {
		ComponentVisual visual = new ComponentVisual();
		try {
			IFile file = ModelManager.instance().getFile(getContent().getModel());
			String id = getContent().getId().toString();
			String xString = file.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, id + "_x"));
			String yString = file.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, id + "_y"));
			
			if (xString != null && yString != null) {
				visual.relocate(Double.parseDouble(xString), Double.parseDouble(yString));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return visual;
	}

	@Override
	protected void doRefreshVisual(ComponentVisual visual) {
		Component comp = getContent();
		visual.setName(comp.getName());

		//setVisualTransform(getContentTransform());
		 
        //visual.getParent().layout();
	}
	
    @Override
    protected void doRemoveContentChild(Object obj) {
    	if (!(obj instanceof Connector))
    		throw new IllegalArgumentException("Argument must be of type Connector");
    	Component comp = (Component) obj;
    	comp.getModel().removeComponent(comp);
    }
    
    @Override
    public Component getContent() {
    	return (Component) super.getContent();
    }

	@Override
	public Affine getContentTransform() {
		return getVisualTransform();
		//Bounds bounds = getVisual().getBoundsInParent();
        //return new Affine(new Translate(bounds.getMinX(), bounds.getMinY()));
	}

	@Override
	public void setContentTransform(Affine totalTransform) {
		setVisualTransform(totalTransform);
	}
}
