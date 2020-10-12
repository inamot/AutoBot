package se.kau.cs.jittac.eclipse.ui.views;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import se.kau.cs.jittac.eclipse.ui.editors.ArchitectureModelEditor;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.feature.Feature;
import se.kau.cs.jittac.model.feature.FeatureLocationRegistry;
import se.kau.cs.jittac.model.mapping.ArchitectureMapping;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IMappableElement;

public class ComponentFeaturesView extends ViewPart implements ISelectionListener {

	private TableViewer viewer;
	private String[] columnHeaders = {"Feature", "#Resources"};
	
	@Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
    }
	
	 public void dispose() {
		 getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		 super.dispose();
	 }
	 
	@Override
	public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
			column.setText(columnHeaders[i]);
			column.pack();
		}
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new ReferenceLabelProvider());
		viewer.setComparator(new LocalViewerComparator());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) return;
        IStructuredSelection selected = (IStructuredSelection) selection;
        if (part instanceof ArchitectureModelEditor) {
        	if (selected.getFirstElement() instanceof Component) {
        		if (!viewer.getControl().isDisposed()) {
        			viewer.setInput((Component) selected.getFirstElement());
        		}
        	}
        }
	}

	private class Entry {
		public Feature feature;
		public int occurences;
		
		public Entry(Feature feature, int occurences) {
			this.feature = feature;
			this.occurences = occurences;
		}
	}
	
	private class ContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement != null && !(inputElement instanceof Component))
				throw new IllegalArgumentException();
		
			if (inputElement != null) {
				Component comp = (Component) inputElement;
				ArchitectureMapping mapping = comp.getModel().getMapping();
				Set<IMappableElement> resources = new HashSet<>();
				Map<Feature, Integer> features = new HashedMap<>();
				for (IMappableElement elem : mapping.getMappedResources(comp)) {
					resources.addAll(mapping.getIdenticallyMappedSubtree(elem));
				}
				for (IMappableElement elem : resources) {
					if (elem instanceof IJittacResource) {
						Set<Feature> featuresInResource = 
								FeatureLocationRegistry.INSTANCE.getFeatures((IJittacResource ) elem);
						for (Feature f : featuresInResource) {
							if (features.containsKey(f)) {
								int nrOfRes = features.get(f);
								features.put(f, nrOfRes + 1);
							}
							else {
								features.put(f, (Integer) 1);
							}
						}
					}
				}
				Entry[] result = new Entry[features.keySet().size()];
				int i = 0;
				for (Feature f : features.keySet()) {
					result[i++] = new Entry(f, features.get(f));
				}
				return result;
			}
			return new Object[0];
		}
	}

	private class ReferenceLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
		   	Entry entry = (Entry) element;
	    	
	    	switch (columnIndex) {
	    	case 0:
	    		return entry.feature.getName();
	        case 1:
	            return Integer.toString(entry.occurences);
	        }
	        return null;
		}
	}
	
	private class LocalViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Entry e = (Entry) e1;
			Entry f = (Entry) e2;
			
			return e.feature.getName().compareTo(f.feature.getName());
		}
	}
	
}
