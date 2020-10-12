package se.kau.cs.jittac.eclipse.builders.jdt;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import se.kau.cs.jittac.model.im.IImplementationModelElement;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.IXReferenceType;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;
import se.kau.cs.jittac.model.mapping.IJittacResource;

public class JDTJavaReference implements IXReference<JDTJavaModelElement, JDTJavaReferenceCodeInformation> {

	private JDTJavaModelElement src, trg;
	private IXReferenceType type;
	private JDTJavaReferenceCodeInformation codeInfo;
	private ImplementationModelPartition part;
	
	
	public JDTJavaReference(JDTJavaModelElement src, 
			JDTJavaModelElement trg, IXReferenceType type, JDTJavaReferenceCodeInformation codeInfo) {
		this.src = src;
		this.trg = trg;
		this.type = type;
		this.codeInfo = codeInfo;
		this.part = src.getPartition();
	}

	public JDTJavaReference(IJavaElement src, 
			IJavaElement trg, IXReferenceType type, JDTJavaReferenceCodeInformation codeInfo,
			ImplementationModelPartition part) {
		this(new JDTJavaModelElement(src, part),
			 new JDTJavaModelElement(trg, part), type, codeInfo);
	}
	
	public JDTJavaReference(IJavaElement src, 
			IJavaElement trg, IXReferenceType type, ASTNode node,
			ImplementationModelPartition part) {
		
		this(src, trg, type, new JDTJavaReferenceCodeInformation(node), part);
	}
	
	@Override
	public ImplementationModelPartition getPartition() {
		return part;
	}
	
	@Override
	public IXReferenceType getType() {
		return type;
	}

	@Override
	public JDTJavaModelElement getSource() {
		return src;
	}

	@Override
	public JDTJavaModelElement getTarget() {
		return trg;
	}

	@Override
	public IJittacResource getResource() {
		return src.getResource();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((codeInfo == null) ? 0 : codeInfo.offset);
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((trg == null) ? 0 : trg.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		JDTJavaReference other = (JDTJavaReference) obj;
		if (codeInfo == null) {
			if (other.codeInfo != null)
				return false;
		} else if (codeInfo.offset !=
				other.codeInfo.offset)
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (trg == null) {
			if (other.trg != null)
				return false;
		} else if (!trg.equals(other.trg))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public String toString() {
		return src.getJavaElement().getElementName() + " -> " + 
				trg.getJavaElement().getElementName() + "; " + 
				type + "; " ;
	}

	@Override
	public  JDTJavaReferenceCodeInformation getInternalReference() {
		return codeInfo;
	}


}
