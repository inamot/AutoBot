package se.kau.cs.jittac.eclipse.ui.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;

import se.kau.cs.jittac.model.am.Connector;

public class MarkConnectorAsEnvisagedOperation extends AbstractOperation implements ITransactionalOperation {

	private Connector conn;
	private boolean wasEnvisaged;
	public MarkConnectorAsEnvisagedOperation(Connector conn) {
		super("Envisage Connector");
		this.conn = conn;
		wasEnvisaged = conn.isEnvisaged();
	}
	
	@Override
	public boolean isContentRelevant() {
		return true;
	}

	@Override
	public boolean isNoOp() {
		return wasEnvisaged;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		conn.setEnvisaged(true);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		conn.setEnvisaged(wasEnvisaged);
		return Status.OK_STATUS;
	}

}
