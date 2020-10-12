package se.kau.cs.jittac.eclipse.builders.jdt.commands;

import static java.util.Collections.emptyList;
import static se.kau.cs.jittac.eclipse.builders.jdt.util.Projects.extractProjectsWithJavaNature;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;

abstract class BaseHandler extends AbstractHandler {
    
    /**
     * Extracts projects from selection for on which this action can be applied.
     * 
     * All other objects are ignored.
     * 
     * @param context evaluation context representing the selection
     * @return collection of projects, never {@code null}
     */
    protected Collection<IProject> extractApplicableProjects(IEvaluationContext context) {
        Object variable = context.getDefaultVariable();
        if (variable instanceof Collection) {
            return extractProjectsWithJavaNature((Collection<?>) variable);
        }

        return emptyList();
    }
    
    /*protected Collection<IProject> extractApplicableProjects(IEvaluationContext context) {
    	
    }*/
    
	protected void fireHadlerChanged(boolean enabledChanged, boolean handledChanged) {
	    fireHandlerChanged(new HandlerEvent(this, enabledChanged, handledChanged));
	}
}
