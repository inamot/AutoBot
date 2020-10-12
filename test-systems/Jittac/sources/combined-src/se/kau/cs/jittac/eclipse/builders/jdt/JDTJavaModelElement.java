package se.kau.cs.jittac.eclipse.builders.jdt;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import se.kau.cs.jittac.model.im.IImplementationModel;
import se.kau.cs.jittac.model.im.IImplementationModelElement;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;
import se.kau.cs.jittac.model.mapping.IJittacProject;
import se.kau.cs.jittac.model.mapping.IJittacResource;
import se.kau.cs.jittac.model.mapping.IJittacResourceFactory;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacProject;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResource;
import se.kau.cs.jittac.model.mapping.eclipse.EclipseJittacResourceFactory;

public class JDTJavaModelElement implements IImplementationModelElement<IJavaElement> {

	private ImplementationModelPartition part;
	private IJavaElement elem;
	private EclipseJittacResource resource;
	private String name;
	
	
	public JDTJavaModelElement(IJavaElement elem,
			ImplementationModelPartition part) {
		this.elem = elem;
		this.part = part;
		resource = EclipseJittacResource.create(elem.getResource(),
				(EclipseJittacProject) part.getImplementationModel().getProject());
	}
	
	public JDTJavaModelElement(IJavaElement elem) {
		
	}
	
	@Override
	public IJittacResource getResource() {
		return resource;
	}

	public IJavaElement getJavaElement() {
		return elem;
	}

	public String toString() {
		try {
			return toPrettyString(elem);
		} catch (JavaModelException e) {
			return elem.getElementName();
		}
		
	}
	
	private static String toPrettyString(IJavaElement element) throws JavaModelException {
		if (element instanceof IType) {
			return ((IType) element).getFullyQualifiedName();
		}
		if (element instanceof IMethod) {
			IMethod method = (IMethod) element;
			return Signature.toString(method.getSignature(),
					method.getElementName(),
					null, false, false);
		}
		if (element instanceof IField) {
			IJavaElement parent = ((IMember) element).getParent();
			IField field = (IField) element;
			return Signature.toString(field.getTypeSignature()) +
					" " + field.getElementName();
					
		}
		if (element instanceof IImportDeclaration ) {
			return ((IImportDeclaration) element).getElementName();
		}
		return element.getElementName();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elem == null) ? 0 : elem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JDTJavaModelElement other = (JDTJavaModelElement) obj;
		if (elem == null) {
			if (other.elem != null)
				return false;
		} else if (!elem.equals(other.elem))
			return false;
		return true;
	}

	@Override
	public IJavaElement getElement() {
		return elem;
	}

	@Override
	public ImplementationModelPartition getPartition() {
		return part;
	}





}
