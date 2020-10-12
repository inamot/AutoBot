package se.kau.cs.jittac.eclipse.builders.jdt;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.MessageFormat;
import java.util.List;

import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;

public class JavaASTHandler extends ASTRequestor {
    private static final MessageFormat progressMessageFormat
            = new MessageFormat("[JITTAC] Extracting Java IA of ''{2}'' project (file {0} of {1}): {3}");

    private IImplementationModel model;
    private IProgressMonitor monitor;
    private int total, processed = 0;
    private boolean processFeatureAnnotations = false;
    
    public JavaASTHandler(IImplementationModel model, int total, IProgressMonitor monitor) {
        this.model = checkNotNull(model);
        this.monitor = checkNotNull(monitor);
        this.total = total;
        activateFeatureAnnotationProcessing();
    }
    
    public void activateFeatureAnnotationProcessing() {
    	processFeatureAnnotations = true;
    }

    public void deactivateFeatureAnnotationProcessing() {
    	processFeatureAnnotations = false;
    }
    
    protected JavaASTProcessorSH createJavaASTProcessor() {
        JavaASTProcessorSH processor = new JavaASTProcessorSH(model);
//        processor.setIgnoreLibraryReferences(
//                preferenceStore().getBoolean(IGNORE_LIBRARY_REFERENCES));
//        processor.setIgnoreIntraProjectReferences(
//                preferenceStore().getBoolean(IGNORE_INTRAPROJECT_REFERENCES));
        processor.setIgnoreLibraryReferences(true);
        processor.setIgnoreIntraProjectReferences(false);

        return processor;
    }
    
    protected FeatureAnnotationProcessor createFeatureAnnotationProcessor() {
    	return new FeatureAnnotationProcessor((EclipseJittacProject) model.getProject());
    }

    private void updateSourcePath(ICompilationUnit source) {
        Object[] arguments = new Object[] {
            processed + 1, total, source.getJavaProject().getElementName(),
            source.getPath().removeLastSegments(1).toPortableString()
        };

        StringBuffer buffer = new StringBuffer(128);
        progressMessageFormat.format(arguments, buffer, null);
        monitor.subTask(buffer.toString());
    }

    @Override
    public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
        updateSourcePath(source);

        try {
        	
            ast.accept(createJavaASTProcessor());
            if (processFeatureAnnotations) {
            	FeatureAnnotationProcessor featureProcessor = createFeatureAnnotationProcessor();
            	featureProcessor.init(source);
            	for (Comment comment : (List<Comment>) ast.getCommentList()) {
            		if (comment instanceof LineComment) { 
            			comment.accept(featureProcessor);
            		}
            	}
            	featureProcessor.end();
            }
            monitor.worked(1);
        } finally {
            //model.clearUnit();
            processed++;
        }
    }
}

