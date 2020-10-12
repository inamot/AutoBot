package se.kau.cs.jittac.eclipse.builders.jdt;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.LineComment;

import se.kau.cs.jittac.model.feature.Feature;
import se.kau.cs.jittac.model.feature.FeatureLocation;
import se.kau.cs.jittac.model.feature.FeatureLocationRegistry;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResource;

/**
 * 
 * @author Sebastian Herold
 *
 * Parser for processing feature annotations as defined in the Florida tool by GU/Chalmers.
 */
public class FeatureAnnotationProcessor extends ASTVisitor {
	
	private EclipseJittacProject project;
	private EclipseJittacResource currentResource;
	private ICompilationUnit source;
	private Feature currentFeature;
	private int currentFeatureStart = -1;
	
	protected static String BEGIN_FEATURE_COMMENT = "&begin\\[\\S+\\]";
	protected static String END_FEATURE_COMMENT = "&end\\[\\S+\\]";
	protected static String SINGLE_LINE_FEATURE_COMMENT = "&line\\[\\S+\\]";

	public FeatureAnnotationProcessor(EclipseJittacProject project) {
		
		this.project = project;
 	}
	
    public void init(ICompilationUnit source) {
    	
        try {
			IResource resource = source.getCorrespondingResource();
			currentResource = EclipseJittacResource.create(resource, project);
			FeatureLocationRegistry.INSTANCE.clear(currentResource);
			this.source = source;
			currentFeature = null;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
    }
    
    public boolean visit(LineComment node) {
    	
    	try {
    		String comment = source.getSource().substring(
    				node.getStartPosition() + 2,
    				node.getStartPosition() + node.getLength()).trim();
    		if (comment.matches(BEGIN_FEATURE_COMMENT)) {
    			handleBeginAnnotation(node, comment);
    		}
    		else if (comment.matches(END_FEATURE_COMMENT)) {
    			handleEndAnnotation(node, comment);
    		}
    		else if (comment.matches(SINGLE_LINE_FEATURE_COMMENT)) {
    			handleSingleLineAnnotation(node, comment);
    		}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

    	return true;
    }
    
    public void end() {
    	
		/*
		 * try { for (FeatureLocation fl :
		 * FeatureLocationRegistry.INSTANCE.getFeatureLocations(currentResource)) {
		 * System.out.println(fl.toString());
		 * System.out.println(source.getSource().substring(fl.getOffset(),
		 * fl.getOffset() + fl.getLength())); } currentResource = null; source = null; }
		 * catch (JavaModelException e) { e.printStackTrace(); }
		 */
    }
    
    protected void handleBeginAnnotation(LineComment node, String comment) {
    	
    	if (!insideFeature()) {
    		String featureName = extractFeatureName(comment);
    		startNewFeatureLocation(featureName, node.getStartPosition());
    	}
    }
    
    protected void handleEndAnnotation(LineComment node, String comment) {
    	
    	if (insideFeature()) {
    		String featureName = extractFeatureName(comment);
    		if (currentFeature.getName().compareTo(featureName) == 0) {
    			createFeatureLocation(node.getStartPosition() + node.getLength() - 1);
    			currentFeature = null;
    			currentFeatureStart = -1;
    		}
    	}
    }
    
    protected void handleSingleLineAnnotation(LineComment node, String comment) {
    	
    	try {
    		int featureLength = 0;
    		String[] nextLines = source.getSource().substring(node.getStartPosition()).split("\n", 3);
    		
    		if (nextLines.length <= 1) return;
    		while (nextLines[1].trim().isEmpty() && !nextLines[2].isEmpty()) {
    			if (nextLines.length == 2) return;
    			if (nextLines[2].isEmpty()) return;
    			featureLength += nextLines[1].length() + 1;
    			String[] temp = nextLines[2].split("\n", 2);
    			nextLines[1] = temp[0];
    			if (temp.length == 2) {
    				nextLines[2] = temp[1];
    			}
    			else {
    				nextLines[2] = "";
    			}
    		}
    		
			if (nextLines.length == 3) {
				String nextLine = nextLines[1];
				//Feature length: node length + line terminator + length of next line - remains of line terminator
				featureLength += node.getLength() + lineTerminatorLength(nextLines[0] + "\n") + nextLine.length() - (lineTerminatorLength(nextLine + "\n") - 1); 
				if (insideFeature()) {
					//Restart position: node start position + feature length + line terminator of code line
					splitCurrentFeature(node, node.getStartPosition() + featureLength + lineTerminatorLength(nextLine + "\n"));
				}
				String featureName = extractFeatureName(comment);
				createFeatureLocation(
						Feature.getFeature(featureName),
						node.getStartPosition(),
						node.getStartPosition() + featureLength - 1);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
    }
    
    protected void splitCurrentFeature(LineComment node, int restartPosition) {
    	
    	int endPosition = latestNonWhiteSpaceBefore(node);
    	if (endPosition != -1) {
    		createFeatureLocation(endPosition);
    		startNewFeatureLocation(currentFeature.getName(), restartPosition);
    	}
	}

    protected void startNewFeatureLocation(String name, int startPosition) {
    	
   		currentFeature = Feature.getFeature(name);
		currentFeatureStart = startPosition;
    }
    
    protected void createFeatureLocation(int endPosition) {
		createFeatureLocation(currentFeature, currentFeatureStart, endPosition);
    }
    
    protected void createFeatureLocation(Feature feature, int startPosition, int endPosition) {
    	
    	int featureLength = endPosition - startPosition + 1;
    	FeatureLocation fl = new FeatureLocation(feature, currentResource, startPosition, featureLength);
    	FeatureLocationRegistry.INSTANCE.registerFeatureLocation(fl);
    }
    
	protected String extractFeatureName(String comment) {
		
    	String[] commentComponents = comment.split("[\\[\\]]");
    	if (commentComponents.length == 2) {
    		return commentComponents[1];
    	}
    	else {
    		return "";
    	}
    }
    
    protected boolean insideFeature() {
    	
    	return currentFeature != null;
    }
    
    private int lineTerminatorLength(String line) {
    	
    	if (line.endsWith("\r\n")) return 2;
    	else if (line.endsWith("\n")) return 1;
    	return 0;
    }
    
    private int latestNonWhiteSpaceBefore(LineComment node) {

		boolean found = false;
		int i = 0;
    	try {
			while (!found) {
				if (node.getStartPosition() - ++i < 0) break;
				if (!Character.isWhitespace(source.getSource().charAt(node.getStartPosition() - i))) {
					found = true;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
    	return found ? node.getStartPosition() - i : -1;
    }
}