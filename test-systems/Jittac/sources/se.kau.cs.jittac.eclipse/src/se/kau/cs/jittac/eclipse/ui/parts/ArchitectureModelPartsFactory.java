package se.kau.cs.jittac.eclipse.ui.parts;

import java.util.Map;

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IContentPartFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

import javafx.scene.Node;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.Connector;

public class ArchitectureModelPartsFactory implements IContentPartFactory {

	@Inject
	private Injector injector;
	
	@Override
	public IContentPart<? extends Node> createContentPart(Object content, Map<Object, Object> contextMap) {
        if (content == null) {
            throw new IllegalArgumentException("Content must not be null!");
        }

        if (content instanceof ArchitectureModel) {
        	return injector.getInstance(ArchitectureModelPart.class);
        } else if (content instanceof Component) {
            return injector.getInstance(ComponentPart.class);
        } else if (content instanceof Connector) {
            return injector.getInstance(ConnectorPart.class);
        } else {
            throw new IllegalArgumentException("Unknown content type <" + content.getClass().getName() + ">");
        }

	}

}
