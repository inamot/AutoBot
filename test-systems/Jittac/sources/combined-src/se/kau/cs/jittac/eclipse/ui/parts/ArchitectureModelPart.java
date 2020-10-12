package se.kau.cs.jittac.eclipse.ui.parts;

import java.util.List;

import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

import javafx.application.Platform;
import javafx.scene.Group;
import se.kau.cs.jittac.eclipse.ui.parts.ConnectorPart.ConnectorPartModelListener;
import se.kau.cs.jittac.model.am.AbstractArchitectureElement;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.am.events.AbstractArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.ConnectorAdditionEvent;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesAddedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesRemovedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorRemovalEvent;
import se.kau.cs.jittac.model.am.events.ConnectorStateChangeEvent;
import javafx.scene.Group;
import javafx.scene.Node;

public class ArchitectureModelPart extends AbstractContentPart<Group> {
	
		private ArchitectureModelPartModelListener modelListener = new ArchitectureModelPartModelListener();
		
		private class ArchitectureModelPartModelListener extends AbstractArchitectureModelChangeListener {
			
			public void onConnectorRemoved(ConnectorRemovalEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						refreshContentChildren();
						refreshVisual();
					}
					
				});
			}
			
			public void onConnectorAdded(ConnectorAdditionEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						refreshContentChildren();
						refreshVisual();
					}
				});
			}
		}
		
	    @Override
	    protected void doAddChildVisual(IVisualPart<? extends Node> child, int index) {
	        getVisual().getChildren().add(child.getVisual());
	    }

	    @Override
	    protected void doAddContentChild(Object contentChild, int index) {
	        if (contentChild instanceof Component) {
	        	Component comp = (Component) contentChild;
	            getContent().createComponent(comp.getName(), comp.getId());
	        }
	        else if (contentChild instanceof Connector) {
	        	Connector con = (Connector) contentChild;
	        	getContent().createConnector(con.getSrc(), con.getTrg());
	        } else {
	            throw new IllegalArgumentException("contentChild has invalid type: " + contentChild.getClass());
	        }
	    }

	    @Override
	    protected Group doCreateVisual() {
	        // the visual is just a container for our child visuals (nodes and
	        // connections)
	        return new Group();
	    }

	    @Override
	    protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
	        return HashMultimap.create();
	    }

	    @Override
	    protected List<? extends Object> doGetContentChildren() {
	    	List<AbstractArchitectureElement> result = Lists.newArrayList();
	    	result.addAll(getContent().getComponentsAsList());
	    	for (Connector con : getContent().getConnectorsAsList()) {
	    		if (!con.isReflexive()) {
	    			result.add(con);
	    		}
	    	}
	        return result;
	    }

	    @Override
	    protected void doRefreshVisual(Group visual) {
	        // no refreshing necessary, just a Group
	    }

	    @Override
	    protected void doRemoveChildVisual(IVisualPart<? extends Node> child, int index) {
	        getVisual().getChildren().remove(child.getVisual());
	    }

	    @Override
	    protected void doRemoveContentChild(Object contentChild) {
	        if (contentChild instanceof Component) {
	            getContent().removeComponent((Component) contentChild);
	        }
	        else if (contentChild instanceof Connector) {
	        	getContent().removeConnector((Connector) contentChild);
	        }
	        else {
	            throw new IllegalArgumentException("contentChild has invalid type: " + contentChild.getClass());
	        }
	    }

	    @Override
	    public ArchitectureModel getContent() {
	        return (ArchitectureModel) super.getContent();
	    }

	   @Override
	    public void setContent(Object content) {
	    	if (content != null) {
	    		if (!(content instanceof ArchitectureModel)) {
	    			return;
	    		}
	    	}
	    	ArchitectureModel oldContent = getContent();
	    	super.setContent(content);
	    	
	    	if (oldContent != null) {
	    		if (!oldContent.equals(content) && oldContent.getModel() != null) {
	    			oldContent.getModel().deregisterListener(modelListener);
	    		}
	    	}
	    	if (content != null) {
	    		if (!content.equals(oldContent)) {
	    			getContent().getModel().registerListener(modelListener);
	    		}
	    	}
	    }
}
