package se.kau.cs.jittac.eclipse.ui.parts;

import org.eclipse.gef.geometry.planar.IGeometry;
import org.eclipse.gef.mvc.fx.providers.GeometricOutlineProvider;

import com.google.inject.Provider;

public class ConnectorGeometricOutlineProvider extends GeometricOutlineProvider implements Provider<IGeometry> {

	@Override
	public IGeometry get() {
		ConnectorPart part = (ConnectorPart) getAdaptable();
		return part.getVisual().getOutline();
	}
}
