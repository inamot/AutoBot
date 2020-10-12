package se.kau.cs.jittac.eclipse.ui.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;
import se.kau.cs.jittac.model.am.Component;

public class SetComponentNameOperation extends AbstractOperation implements ITransactionalOperation {

	private ComponentPart part;
	private String oldName, newName;
	
	public SetComponentNameOperation(ComponentPart part, String newName) {
		super("Change component name");
		this.part = part;
		this.newName = newName;
		this.oldName = part.getContent().getName();
	}

	@Override
	public boolean isContentRelevant() {
		return true;
	}

	@Override
	public boolean isNoOp() {
		return newName.equals(oldName);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Component comp = part.getContent();
		comp.getModel().setComponentName(comp, newName);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		part.getContent().setName(oldName);
		return Status.OK_STATUS;
	}

}
