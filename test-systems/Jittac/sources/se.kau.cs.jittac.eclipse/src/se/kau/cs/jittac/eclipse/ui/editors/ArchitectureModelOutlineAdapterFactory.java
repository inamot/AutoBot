package se.kau.cs.jittac.eclipse.ui.editors;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class ArchitectureModelOutlineAdapterFactory implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (IContentOutlinePage.class.equals(adapterType)) {
			ArchitectureModelEditor editor = (ArchitectureModelEditor) adaptableObject;
			return (T) new ArchitectureModelOutlinePage(editor);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] {IContentOutlinePage.class};
	}

}
