package se.kau.cs.jittac.eclipse.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import se.kau.cs.jittac.eclipse.builders.jdt.JDTJavaReference;
import se.kau.cs.jittac.eclipse.builders.jdt.JDTJavaReferenceCodeInformation;
import se.kau.cs.jittac.eclipse.ui.editors.ArchitectureModelEditor;
import se.kau.cs.jittac.model.am.Connector;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResourceFactory;

public class ReferenceTableView extends ViewPart implements ISelectionListener {

	private TableViewer viewer;
	private Connector connector;
	private LocalComparator comparator;
	
	private String[] columnHeaders = {"Source Resource", "Source", "Type", "Target", "Target Resource"};
	private double[] widthFactors = {0.15, 0.3, 0.1, 0.3, 0.15};
	
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
        int width = viewer.getTable().getSize().x;
		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
			column.setText(columnHeaders[i]);
			column.setWidth((int) (width * widthFactors[i])); 
			column.addSelectionListener(getSelectionAdapter(column, i));
			column.pack();
		}
		comparator = new LocalComparator();
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new ReferenceLabelProvider());
		viewer.setComparator(comparator);
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IXReference xref = (IXReference) ((IStructuredSelection)event.getSelection()).getFirstElement();
				IJittacResource jResource = xref.getResource();
				if (jResource.getResourceModelName()
						.equals(EclipseJittacResourceFactory.RESOURCE_MODEL_NAME)) {
					IResource resource = ((EclipseJittacResource) jResource).getWrappedResource();
					if (resource.getType() != IResource.FILE)
	                	return;
					if (xref instanceof JDTJavaReference) {
						JDTJavaReferenceCodeInformation refInf =
								((JDTJavaReference) xref).getInternalReference();
						try {
		                    IEditorPart editor = IDE.openEditor(getSite().getPage(), (IFile) resource, true);
		                    ((ITextEditor) editor).selectAndReveal(refInf.offset, refInf.length);
		                } catch (PartInitException e) {
		                    e.printStackTrace();
		                }
					}
				}
				
			}
			
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private class ContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement != null && !(inputElement instanceof Connector))
				throw new IllegalArgumentException();
		
			if (inputElement != null) {
				Connector conn = (Connector) inputElement;
				Object result[] = conn.getContributingReferences().toArray();
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
		   	IXReference xref = (IXReference) element;
	    	
	    	return referenceToColumnText(xref, columnIndex);
		}
		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) return;
        IStructuredSelection selected = (IStructuredSelection) selection;
        if (part instanceof ArchitectureModelEditor) {
        	if (selected.getFirstElement() instanceof Connector) {
        		if (!viewer.getControl().isDisposed()) {
        			viewer.setInput((Connector) selected.getFirstElement());
        		}
        	}
        }
	}
	
	private class LocalComparator extends ViewerComparator {
		
		private static final int DESCENDING  = 1;
		private int currentColumn = 0;
		private int direction = DESCENDING;
		
		public int getDirection() {
			return direction == DESCENDING ? SWT.DOWN : SWT.UP;
		}
		
		public void setColumn(int column) {
			if (currentColumn == column) {
				direction = -direction; 
			}
			else {
				currentColumn = column;
			}
		}
		
		@Override
	    public int compare(Viewer viewer, Object e1, Object e2) {
		
			String s1 = referenceToColumnText((IXReference<?,?>) e1, currentColumn);
			String s2 = referenceToColumnText((IXReference<?,?>) e2, currentColumn);
			return -direction * s1.compareTo(s2);
		}
	}
	
	private String referenceToColumnText(IXReference<?,?> ref, int columnIndex) {
    	switch (columnIndex) {
    	case 0:
    		return ref.getSource().getResource().toString();
        case 1:
            return ref.getSource().toString();
        case 2:
        	return ref.getType().name();	            
        case 3:
            return ref.getTarget().toString();
        case 4:
        	return ref.getTarget().getResource().toString();
        }
        return null;
	}
	
    private SelectionAdapter getSelectionAdapter(final TableColumn column,
            final int index) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comparator.setColumn(index);
                int dir = comparator.getDirection();
                viewer.getTable().setSortDirection(dir);
                viewer.getTable().setSortColumn(column);
                viewer.refresh();
            }
        };
        return selectionAdapter;
    }
}
