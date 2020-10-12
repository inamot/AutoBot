package se.kau.cs.jittac.model.am.events;

import se.kau.cs.jittac.model.am.ArchitectureModel;

public interface IArchitectureModelVisitor {
	
	public void visit(ArchitectureModel model);

}
