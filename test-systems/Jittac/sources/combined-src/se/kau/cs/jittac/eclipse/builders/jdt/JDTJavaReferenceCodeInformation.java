package se.kau.cs.jittac.eclipse.builders.jdt;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JDTJavaReferenceCodeInformation {

	public final int offset;
	public final int length;
	public final int line;
	
	public JDTJavaReferenceCodeInformation(ASTNode node) {
		offset = node.getStartPosition();
		length = node.getLength();
		line = ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition());
	}
	
	public JDTJavaReferenceCodeInformation(int offset, int length, int line) {
		this.offset = offset;
		this.length = length;
		this.line = line;
	}
}
