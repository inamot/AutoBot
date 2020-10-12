package se.kau.cs.jittac.eclipse.builders.jdt;

import se.kau.cs.jittac.model.im.ImplementationModelPartitionType;

public class JDTJavaImplementationModelPartitionType 
implements ImplementationModelPartitionType {

	private static final String NAME = "JDT";
	public static final JDTJavaImplementationModelPartitionType INSTANCE =
			new JDTJavaImplementationModelPartitionType();
	
	private JDTJavaImplementationModelPartitionType() {
	}

	@Override
	public Class<JDTJavaModelElement> getElementType() {
		return JDTJavaModelElement.class;
	}

	@Override
	public Class<JDTJavaReference> getReferenceType() {
		return JDTJavaReference.class;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
