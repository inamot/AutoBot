package se.kau.cs.jittac.eclipse.ui.editors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.Component;
import se.kau.cs.jittac.model.am.events.AbstractArchitectureModelChangeListener;
import se.kau.cs.jittac.model.am.events.ComponentAdditionEvent;
import se.kau.cs.jittac.model.am.events.ComponentNameChangeEvent;
import se.kau.cs.jittac.model.am.events.ComponentRemovalEvent;
import se.kau.cs.jittac.model.am.events.IArchitectureModelChangeListener;
import se.kau.cs.jittac.model.mapping.IMappableElement;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResource;

public class ArchitectureModelOutlinePage extends ContentOutlinePage {

	private ArchitectureModelEditor editor;
	
	private IArchitectureModelChangeListener architectureModelListener = 
			new AbstractArchitectureModelChangeListener() {
		@Override
		public void onComponentAdded(ComponentAdditionEvent event) {
			getTreeViewer().refresh(false);
		}
		
		@Override
		public void onComponentRemoved(ComponentRemovalEvent event) {
			getTreeViewer().refresh(false);
		}
		
		@Override
		public void onComponentNameChanged(ComponentNameChangeEvent event) {
			getTreeViewer().refresh(event.getComponent());
		}
	};

	public ArchitectureModelOutlinePage(ArchitectureModelEditor editor) {
		super();
		this.editor = editor;

	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new OutlineContentProvider());
		viewer.setLabelProvider(new OutlineLabelProvider());
		viewer.setInput(editor.getModel());
		viewer.expandAll();
		
		ArchitectureModel model = (ArchitectureModel) editor.getContentViewer().getContents().get(0);
		model.registerListener(architectureModelListener);
		
		int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
		//DropTarget target = new DropTarget(getTreeViewer().getTree(), operations);
		ResourceTransfer resourceTransfer = ResourceTransfer.getInstance();
		//FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer transferTypes[] = new Transfer[] {resourceTransfer};
		
		getTreeViewer().addDropSupport(operations, transferTypes, new DropTargetAdapter() {
			
			public void drop(DropTargetEvent event) {
				
				IMappableElement newMapElement = null;
				Component comp = null;
				
				
				if (event.data instanceof IResource[]) {
					IResource dataArr[] = (IResource []) event.data;
					for (IResource res : dataArr) {
						if (res instanceof IProject) {
							newMapElement = EclipseJittacProject.get((IProject) res);
						}
						else if (res instanceof IResource) {
							newMapElement = EclipseJittacResource.create((IResource) res,
									EclipseJittacProject.get(((IResource) res).getProject()));
						}
						else return;
						
						TreeItem item = (TreeItem) event.item;
						Object data = item.getData();
						if (data instanceof ArchitectureModel) {
							
						}
						else if (data instanceof Component) {
							comp = (Component) data;
							comp.getModel().getMapping().addMapping(comp, newMapElement);
						}
						else if (data instanceof IMappableElement) {
							IMappableElement mappedElem = 
									(IMappableElement) data;
							comp = editor.getModel().getMapping().getComponent(mappedElem);
							comp.getModel().getMapping().addMapping(comp, newMapElement);
						}
						else {
							//Do nothing I suppose
						}
					}
				}
				if (comp != null)
					getTreeViewer().refresh(comp);
			}
			
			public void dragEnter(DropTargetEvent event) {
				if (event.detail != DND.DROP_COPY) {
					event.detail = DND.DROP_COPY;
				}
			}
		});
		
		getTreeViewer().getTree().addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					for (Object selected : getTreeViewer().getStructuredSelection().toList())  {
						if (selected instanceof IMappableElement) {
							getTreeViewer().setSelection(null);
							IMappableElement element = (IMappableElement) selected;
							Component comp = editor.getModel().getMapping().getComponent(element);
							editor.getModel().getMapping().removeMapping(comp, element);
							getTreeViewer().refresh(comp);
							
						}

					}
					
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		});
	}
	
	@Override
	public void dispose() {
		ArchitectureModel model = (ArchitectureModel) editor.getContentViewer().getContents().get(0);
		model.deregisterListener(architectureModelListener);

		super.dispose();
	}
	
	public class OutlineContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof ArchitectureModel) {
				return getChildren(inputElement);
			}
			return null;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ArchitectureModel) {
				return ((ArchitectureModel) parentElement).getComponents().toArray();
			}
			else if (parentElement instanceof Component) {
				Component comp = (Component) parentElement;
				return comp.getModel().getMapping().getMappedResources(comp).toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof Component) {
				return ((Component) element).getModel();
			}
			else if (element instanceof IMappableElement) {
				ArchitectureModel model = ArchitectureModelOutlinePage.this.editor.getModel();
				return model.getMapping().getComponent((IMappableElement) element);
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ArchitectureModel) {
				return ((ArchitectureModel) element).getComponents().size() > 0;
			}
			if (element instanceof Component) {
				Component comp = (Component) element;
				return comp.getModel().getMapping().getMappedResources(comp).size() > 0;
			}
			return false;
		}
		
	}
	
	public class OutlineLabelProvider implements ILabelProvider {

		private ILabelProvider peLabelProvider;
		
		public OutlineLabelProvider() {
			IViewPart view;
			try {
				view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().
						getActivePage().showView(ProjectExplorer.VIEW_ID);
				peLabelProvider = (ILabelProvider) ((ProjectExplorer) view).getCommonViewer().getLabelProvider();
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getImage(Object element) {
			//TODO: find out how to properly reuse icons and decorations from ProjectExplorer and edit constructor
			//view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ProjectExplorer.VIEW_ID);
			//NavigatorContentService contentService  = new NavigatorContentService(ProjectExplorer.VIEW_ID);
			if (element instanceof Component) {
				
			}
			if (element instanceof EclipseJittacResource) {
				return peLabelProvider.getImage(((EclipseJittacResource) element).getWrappedResource());
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ArchitectureModel)
			{
				return "Mappings";
			}
			else if (element instanceof Component) {
				return ((Component) element).getName() + " mappings";
			}
			else if (element instanceof EclipseJittacProject ) {
				return peLabelProvider.getText(((EclipseJittacProject) element).getWrappedProject());
			}
			else if (element instanceof EclipseJittacResource) {
				return peLabelProvider.getText(((EclipseJittacResource) element).getWrappedResource());				
			}
			return null;
		}
		
		
		
	}

}
