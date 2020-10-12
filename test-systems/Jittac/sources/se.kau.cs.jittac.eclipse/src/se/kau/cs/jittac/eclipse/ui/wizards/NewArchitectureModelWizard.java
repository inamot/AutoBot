package se.kau.cs.jittac.eclipse.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

import se.kau.cs.jittac.eclipse.Activator;
import se.kau.cs.jittac.model.am.ArchitectureModel;
import se.kau.cs.jittac.model.am.io.ArchitectureModelWriter;
import se.kau.cs.jittac.model.mapping.JittacResourceModelRegistry;

/**
 * This is the Eclipse wizard for creating new architectural model files.
 * @author Jacek Rosik, Sebastian Herold
 *
 */
public class NewArchitectureModelWizard extends Wizard implements INewWizard {

	private WizardNewFileCreationPage page;
	private IStructuredSelection selection;
	
	public NewArchitectureModelWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public void addPages() {
		super.addPages();
		page = new WizardNewFileCreationPage("newFilePage", selection);
		page.setTitle("New Architecture Model");
		page.setDescription("Create an empty architecture model file for analysis in Jittac.");
		page.setFileExtension("xam");
		page.setFileName("architecture.xam");
	    addPage(page);
	}
	
	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final IPath containerName = page.getContainerFullPath();
		final String fileName = page.getFileName();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(IPath containerPath, String filename,	IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + filename, 2);
		
		// Get the container.
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(containerPath);
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerPath.toOSString() + "\" does not exist.");
		}
		
		// Create file.
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(filename));

		try {
			ArchitectureModel newModel = ArchitectureModel.createArchitectureModel();
			ByteArrayOutputStream output = new ByteArrayOutputStream(); 
			ArchitectureModelWriter.write(output, newModel);
			ByteArrayInputStream input =  new ByteArrayInputStream(output.toByteArray());
					
			if (file.exists()) {

				file.setContents(input, true, true, monitor);
			} else {
				file.create(input, true, monitor);
			}
			
			input.close();
			output.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		
		// Open file.
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, null);
		throw new CoreException(status);
	}

}
