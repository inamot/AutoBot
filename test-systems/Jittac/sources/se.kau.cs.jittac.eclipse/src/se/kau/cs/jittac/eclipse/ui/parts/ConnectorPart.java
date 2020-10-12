package se.kau.cs.jittac.eclipse.ui.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.fx.anchors.IAnchor;
import org.eclipse.gef.mvc.fx.parts.AbstractContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Provider;

import javafx.application.Platform;
import javafx.scene.Node;
import se.kau.cs.jittac.eclipse.ui.visuals.ConnectorVisual;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.am.events.AbstractArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesAddedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorReferencesRemovedEvent;
import se.kau.cs.jittac.model.am.events.ConnectorRemovalEvent;
import se.kau.cs.jittac.model.am.events.ConnectorStateChangeEvent;

public class ConnectorPart extends AbstractContentPart<ConnectorVisual> {

	    private static final String START_ROLE = "START";
	    private static final String END_ROLE = "END";
	    
	    private ConnectorPartModelListener modelListener = new ConnectorPartModelListener();

   
	    public class ConnectorPartModelListener extends AbstractArchitectureModelChangeListener {
	    	
	    	@Override
	    	public void onConnectorReferencesAdded(ConnectorReferencesAddedEvent event) {
	    		if (event.getModifiedConnector().equals(getContent())) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							refreshVisual();
						}
						
					});
	    		}
	    	}
	    	
	    	@Override
	    	public void onConnectorReferencesRemoved(ConnectorReferencesRemovedEvent event) {
	    		if (event.getModifiedConnector().equals(getContent())) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							refreshVisual();
						}
						
					});
	    		}
	    	}
	    	
	    	@Override
	    	public void onConnectorStateChange(ConnectorStateChangeEvent event) {
	    		if (event.getConnector().equals(getContent())) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							refreshVisual();
						}
						
					});
	    		}
	    	}
	    }
	    
	    @Override
	    protected void doAttachToAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
	        // find a anchor provider, which must be registered in the module
	        // be aware to use the right interfaces (Provider is used a lot)
	        @SuppressWarnings({ "serial", "restriction" })
	        Provider<? extends IAnchor> adapter = anchorage
	                .getAdapter(AdapterKey.get(new TypeToken<Provider<? extends IAnchor>>() {
	                }));
	        if (adapter == null) {
	            throw new IllegalStateException("No adapter  found for <" + anchorage.getClass() + "> found.");
	        }
	        IAnchor anchor = adapter.get();

	        if (role.equals(START_ROLE)) {
	            getVisual().getConnection().setStartAnchor(anchor);
	        } else if (role.equals(END_ROLE)) {
	            getVisual().getConnection().setEndAnchor(anchor);
	        } else {
	            throw new IllegalArgumentException("Invalid role: " + role);
	        }
	    }

	    @Override
	    protected ConnectorVisual doCreateVisual() {

	        return new ConnectorVisual();
	    }

	    @Override
	    protected void doDetachFromAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
	        if (role.equals(START_ROLE)) {
	            getVisual().getConnection().setStartPoint(getVisual().getConnection().getStartPoint());
	        } else if (role.equals(END_ROLE)) {
	            getVisual().getConnection().setEndPoint(getVisual().getConnection().getEndPoint());
	        } else {
	            throw new IllegalArgumentException("Invalid role: " + role);
	        }
	    }
	    
/*	    @Override
	    protected void doDetachFromContentAnchorage(Object contentAnchorage, String role) {
	    	
	    }*/

	    @Override
	    protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
	        SetMultimap<Object, String> anchorages = HashMultimap.create();

	        if (getContent().getModel() != null) {
	        	anchorages.put(getContent().getSrc(), START_ROLE);
	        	anchorages.put(getContent().getTrg(), END_ROLE);
	        }
	        return anchorages;
	    }

	    @Override
	    protected List<? extends Object> doGetContentChildren() {
	        return Collections.emptyList();
	    }

	/*    @Override
	    protected void doRemoveContentChild(Object obj) {
	    	if (!(obj instanceof Connector))
	    		throw new IllegalArgumentException("Argument must be of type Connector");
	    	Connector conn = (Connector) obj;
	    	conn.getModel().removeConnector(conn);
	    }*/
	    
	    @Override
	    protected void doRefreshVisual(ConnectorVisual visual) {
			Connector con = getContent();
			String s = Integer.toString(con.getContributingReferences().size());
			visual.setLabel(s);
			//setVisualTransform(getContentTransform());
			//set stroke based on content 
			visual.setStrokeStyle(con.getContributingReferences().size(), con.isEnvisaged());
	       // visual.getParent().layout();
	    }

	    @Override
	    public Connector getContent() {
	    	return (Connector) super.getContent();
	    }
	    
	    @Override
	    public void setContent(Object content) {
	    	if (content != null) {
	    		if (!(content instanceof Connector)) {
	    			return;
	    		}
	    	}
	    	Connector oldContent = getContent();
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
