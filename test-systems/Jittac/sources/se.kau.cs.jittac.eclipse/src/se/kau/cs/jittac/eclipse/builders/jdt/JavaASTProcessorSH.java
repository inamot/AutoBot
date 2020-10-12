package se.kau.cs.jittac.eclipse.builders.jdt;

import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.jdt.core.IPackageFragmentRoot.K_BINARY;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import se.kau.cs.jittac.model.feature.Feature;
import se.kau.cs.jittac.model.feature.FeatureLocation;
import se.kau.cs.jittac.model.feature.FeatureLocationRegistry;
import se.kau.cs.jittac.model.im.BuildUnit;
import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.IXReferenceType;
import se.kau.cs.jittac.model.im.ImplementationModelFactory;
import se.kau.cs.jittac.model.im.ImplementationModelFactoryRegistry;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;



/**
 * 
 * @author Jacek Rosik, Sebastian Herold
 *
 */
public class JavaASTProcessorSH extends ASTVisitor {

    private boolean ignoreLibraryReferences = false;
    private boolean ignoreIntraProjectReferences = false;
 
    private ImplementationModelPartition part = null;
    private ImplementationModelFactory<JDTJavaModelElement, IJavaElement,
    	JDTJavaReference, JDTJavaReferenceCodeInformation> factory = null;
    private BuildUnit unit = null;    
    //By setting the following flag to true, the visitors methods for types
    //can be deactivated; they are called but return without executing
    //any further actions.
    //Usage: when visiting a parent node that contains types as children you don't
    //want to visit, set flag to true in the visit() method of the parent node. Reset to 
    // false in the corresponding endvisit() method.
    //This is required if the relationship to the type is processed in the parent node and
    //visiting the type is unnecessary/redundant. See visit(VariableDeclarationFragment).
    private boolean ignoreTypesFlag = false;

    class NodeBinding {
        final ASTNode node;
        final IJavaElement element;
        
        public NodeBinding(ASTNode node, IJavaElement element) {
            this.node = node;
            this.element = element;
        }
    }
    private Stack<NodeBinding> _stack = new Stack<NodeBinding>();

    public JavaASTProcessorSH(IImplementationModel model) {
    	if (model == null)
    		throw new IllegalArgumentException("ImplementationModel not set.");
        
    	part = model.getPartitionForBuilderType(JDTJavaImplementationModelPartitionType.INSTANCE);
        factory = ImplementationModelFactoryRegistry.instance().getFactory(JDTJavaImplementationModelPartitionType.INSTANCE);
        if (factory == null) {
        	throw new IllegalArgumentException("Implementation factory for JDT not found.");
        } 
    }

    public boolean isIgnoreLibraryReferences() {
        return ignoreLibraryReferences;
    }

    public void setIgnoreLibraryReferences(boolean value) {
        this.ignoreLibraryReferences = value;
    }


    public boolean isIgnoreIntraProjectReferences() {
        return ignoreIntraProjectReferences;
    }


    public void setIgnoreIntraProjectReferences(boolean value) {
        this.ignoreIntraProjectReferences = value;
    }

    protected IJavaElement currentBinding() {
        return _stack.peek().element;
    }
    
    protected boolean pushNodeAndBinding(ASTNode node, IBinding binding) {
        if (binding == null) {
            _stack.push(new NodeBinding(node, null));
            unhandledBinding(node);
            return false;
        } else
            _stack.push(new NodeBinding(node, binding.getJavaElement()));

        return true;
    }
    
    protected void popNodeAndBinding() {
        _stack.pop();
    }

    public boolean visit(CompilationUnit node) {
        IJavaElement element = node.getJavaElement();
        _stack.push(new NodeBinding(node, element));
        try {
			IResource resource = ((ICompilationUnit) node.getJavaElement()).getCorrespondingResource();
			EclipseJittacResource jResource = EclipseJittacResource.create(resource, 
					(EclipseJittacProject) part.getImplementationModel().getProject());
			unit = part.startUnit(jResource);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return true;
    }

	public void endVisit(CompilationUnit node) {
        popNodeAndBinding();
		IResource resource = null;
		try {
			resource = ((ICompilationUnit) node.getJavaElement()).getCorrespondingResource();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Finish resource processing and flush all the changes.
        part.endUnit(unit);
    }
    
    public boolean visit(AnonymousClassDeclaration node) {
    	//SH: containment.
    	this.handleXReference(IXReferenceType.CONTAINMENT,
    			currentBinding(),
    			node.resolveBinding().getJavaElement(),
    			node);
        return pushNodeAndBinding(node, node.resolveBinding());
    }
    
    public void endVisit(AnonymousClassDeclaration node) {
        popNodeAndBinding();
    }
    
    public boolean visit(MethodDeclaration node) {
    	//SH: containment.
    	this.handleXReference(IXReferenceType.CONTAINMENT,
    			currentBinding(),
    			node.resolveBinding().getJavaElement(),
    			node);
        return pushNodeAndBinding(node, node.resolveBinding());
    }
    
    public void endVisit(MethodDeclaration node) {
        popNodeAndBinding();
    }
    
    
    public boolean visit(TypeDeclaration node) {
    	//SH: containment.
     	this.handleXReference(IXReferenceType.CONTAINMENT,
    			currentBinding(),
    			node.resolveBinding().getJavaElement(),
    			node);
        return pushNodeAndBinding(node, node.resolveBinding());
    }
    
    public void endVisit(TypeDeclaration node) {
        popNodeAndBinding();
    }
    
    
    public boolean visit(ImportDeclaration node) {
        IBinding binding = node.resolveBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }
        
        handleXReference(IXReferenceType.IMPORT, currentBinding(),
                         binding.getJavaElement(), node.getName());
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public boolean visit(MethodInvocation node) {
        IMethodBinding binding = node.resolveMethodBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }

        IJavaElement target = binding.getJavaElement();
        if (target == null && binding.getDeclaringClass().isEnum()) {
           // In case of enums some standard methods (like 'values()' will not resolve.
           // Reference the enum itself then..
           target = binding.getDeclaringClass().getJavaElement();
        }
        
        handleXReference(IXReferenceType.CALL, currentBinding(), target, node);
        
        // Process the expression which give the element
        // on which the method is called. 
        if (node.getExpression() != null)
            node.getExpression().accept(this);
        
        // Process type arguments (for templates).
        Iterator<ASTNode> types = node.typeArguments().iterator();
        while (types.hasNext())
            types.next().accept(this);
        
        // And process all the arguments.
        Iterator<ASTNode> args = node.arguments().iterator();
        while (args.hasNext())
            args.next().accept(this);

        return false;
    }
    
    @SuppressWarnings("unchecked")
    public boolean visit(SuperMethodInvocation node) {
        IBinding binding = node.resolveMethodBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }
        
        handleXReference(IXReferenceType.CALL, currentBinding(), binding.getJavaElement(),node);
        
        // Process type arguments (for templates).
        Iterator<ASTNode> types = node.typeArguments().iterator();
        while (types.hasNext())
            types.next().accept(this);
        
        // And process all the arguments.
        Iterator<ASTNode> args = node.arguments().iterator();
        while (args.hasNext())
            args.next().accept(this);

        return false;
    }
    
    @SuppressWarnings("unchecked")
    public boolean visit(ClassInstanceCreation node) {
        // TODO: Investigate why resolveConstructorBinding() doesn't work.

        ITypeBinding binding = node.getType().resolveBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }
            
        handleXReference(IXReferenceType.ACCESS,currentBinding(),
                         binding.getJavaElement(), node);
        
        // Process the expression which give the element
        // on which the method is called.
        if (node.getExpression() != null)
            node.getExpression().accept(this);
        
        // Process type arguments (for templates).
        Iterator<ASTNode> types = node.typeArguments().iterator();
        while (types.hasNext())
            types.next().accept(this);
        
        // And process all the arguments.
        Iterator<ASTNode> args = node.arguments().iterator();
        while (args.hasNext())
            args.next().accept(this);

        return false;
    }
    
    @SuppressWarnings("unchecked")
    public boolean visit(SuperConstructorInvocation node) {
        IMethodBinding binding = node.resolveConstructorBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }
        
        IJavaElement target = binding.getJavaElement();
        if (target == null 
            && binding.isConstructor()
            && node.arguments().size() == 0) {
            // In case of a default constructor reference the Class itself...
            target = binding.getDeclaringClass().getJavaElement();
        }
        handleXReference(IXReferenceType.CALL, currentBinding(), target, node);
        
        // Process the expression which give the element
        // on which the method is called.
        if (node.getExpression() != null)
            node.getExpression().accept(this);
        
        // Process type arguments (for templates).
        Iterator<ASTNode> types = node.typeArguments().iterator();
        while (types.hasNext())
            types.next().accept(this);
        
        // And process all the arguments.
        Iterator<ASTNode> args = node.arguments().iterator();
        while (args.hasNext())
            args.next().accept(this);

        return false;
    }   
    
    public boolean visit(FieldDeclaration node) {
    	//Types will be dealt with while visiting the declaration fragments
    	//Thus, disable the type visits here.
    	ignoreTypesFlag = true;
    	return true;
    }
    
    public void endVisit(FieldDeclaration node) {
    	ignoreTypesFlag = false;
    }
    
    public boolean visit(VariableDeclarationStatement node) {
    	//Types will be dealt with while visiting the declaration fragments
    	ignoreTypesFlag = true;
    	return true;
    }

    public void endVisit(VariableDeclarationStatement node) {
    	ignoreTypesFlag = false;
    }
    
    public boolean visit(VariableDeclarationFragment node) {
    	IVariableBinding binding = node.resolveBinding();
     	
    	if (binding == null) {
    		unhandledBinding(node);
    		return false;
    	}
    	handleXReference(IXReferenceType.CONTAINMENT, currentBinding(), binding.getJavaElement(), node);
		ITypeBinding typeBinding = binding.getType();
        if (typeBinding == null) {
            unhandledBinding(node);
        }
        else {
        	handleXReference(IXReferenceType.TYPEREF, binding.getJavaElement(), typeBinding.getJavaElement(), node);
        }
    	return true;
    }

    public boolean visit(FieldAccess node) {
    	IVariableBinding binding = node.resolveFieldBinding();
    	
    	if (binding == null) {
    		unhandledBinding(node);
    	}
    	else {
    		handleXReference(IXReferenceType.ACCESS, currentBinding(), binding.getJavaElement(), node);
    	}
    	return true;
    }
    
    public boolean visit(Assignment node) {
        // Get the thing we assign to...
        IBinding binding = null;
        boolean handledLeftHand = false;
        Expression expr = node.getLeftHandSide();
        while (expr != null) {
            if (expr instanceof Name) {
                binding = ((Name) expr).resolveBinding();
                break;
            } else if (expr instanceof FieldAccess) {
                FieldAccess fa = (FieldAccess) expr;
                fa.getExpression().accept(this);
                binding = fa.resolveFieldBinding();
                break;
            } else if (expr instanceof SuperFieldAccess) {
                SuperFieldAccess fa = (SuperFieldAccess) expr;
                Name qualifier = fa.getQualifier();
                if (qualifier != null) {
                    qualifier.accept(this);
                }
                binding = fa.resolveFieldBinding();
                break;
            } else if (expr instanceof ArrayAccess) {
                ArrayAccess aa = (ArrayAccess) expr;
                aa.getIndex().accept(this);
                expr = aa.getArray();
                continue;
            } else if (expr instanceof ParenthesizedExpression) {
                expr = ((ParenthesizedExpression) expr).getExpression();
                continue;
            } else if (expr instanceof MethodInvocation
                       || expr instanceof SuperMethodInvocation) {
                expr.accept(this);
                handledLeftHand = true;
                break;
            }

//            warn("Unable to handle left-hand side of assignment expression: {0}[''{1}'']",
//                 expr.getClass().getSimpleName(), expr.toString());
            break;
        }
        
        if (!handledLeftHand) {
            if (binding == null) {
                unhandledBinding(node);
            }
            handleXReference(IXReferenceType.ASSIGNMENT, currentBinding(),
                             binding.getJavaElement(), node);
        }
        
        // Process right hand side expression (value).
        node.getRightHandSide().accept(this);
        return false;
    }
    
    public boolean visit(SimpleName node) {
        handleName(node);
        return false;
    }

    public boolean visit(QualifiedName node) {
        handleName(node);
        return false;
    }
    
    public boolean visit(SimpleType node) {
    	
    	return ignoreTypesFlag? true : handleType(node);
    }

    public boolean visit(QualifiedType node) {
    	return ignoreTypesFlag? true : handleType(node);
    }

    //Creates a reference between the currently bound type at the top of the
    //stack and the type given by node
    private boolean handleType(Type node) {
    	ITypeBinding binding = node.resolveBinding();
    	if (binding == null) {
    		unhandledBinding(node);
    		return false;
    	}
    	handleXReference(IXReferenceType.TYPEREF, currentBinding(), binding.getJavaElement(), node);
    	return true;
    }
    
    private boolean handleName(Name node) {
        return handleName(node, IXReferenceType.ACCESS);
    }

    private boolean handleName(Name node, IXReferenceType type) {
        IBinding binding = node.resolveBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }

        switch (binding.getKind()) {
//        case IBinding.METHOD:
//        case IBinding.TYPE:
        case IBinding.VARIABLE:
            // Ignore local variables and self references.
            IJavaElement dependant = (IJavaElement) currentBinding();
            IJavaElement dependency = binding.getJavaElement();
            if (dependency == null || dependant == null) {
                // This seems to happen mostly for array length parameter.              
                // TODO: Investigate a little more.
                return false;
            }
//            if (dependency.getElementType() == IJavaElement.LOCAL_VARIABLE
//                || dependant.equals(dependency))
//                break;
            if (dependency.getElementType() == IJavaElement.LOCAL_VARIABLE) {
            	break;
            }
            handleXReference(type, dependant, dependency, node);
            break;
            
        case IBinding.PACKAGE:
            // IGNORE THESE
            break;

        default:
            //unhandledNode(node);
        }
        
        return true;
    }

    public void handleXReference(IXReferenceType type, IJavaElement source, IJavaElement target, ASTNode node) {
        if (source == null || target == null) {
            // TODO: Sometimes some binding may fail to resolve into java elements. Fix it!
//            error("Failed to resolve Java element for {0}[''{1}'']",
//                  node.getClass().getSimpleName(), node.toString());
            return;
        }
        
        IResource sourceResource = source.getResource();
        
        IResource targetResource = target.getResource();
        
        // Ignore references within ghe same project.
        if (ignoreIntraProjectReferences && targetResource != null
            && sourceResource.getProject().equals(targetResource.getProject())) {
            return;
        } 
        
        // Ignore all references to the types contained in libraries (jar or zip files)
        if (ignoreLibraryReferences) {
            try {
                IPackageFragmentRoot root = (IPackageFragmentRoot) target.getAncestor(PACKAGE_FRAGMENT_ROOT);
                if (root == null || root.getKind() == K_BINARY) {
                    return;
                }
            } catch (JavaModelException ex) {
//                error(ex, "Unexpected exception in AST Parser");
            }
        }
        
        JDTJavaReference ref = factory.createReference(
        		factory.createElement(source, part),
                factory.createElement(target, part),
                type,
                new JDTJavaReferenceCodeInformation(node));
        part.addReference(ref);
    }   
    
    private void unhandledBinding(ASTNode node) {
//        warn("Unhandled Binding ({0}): {1}", node.getClass().getSimpleName(), node.toString());
    }

    private void unhandledNode(ASTNode node){
//        warn("Unhandled Node: {0}",  node.toString());
    }
}

