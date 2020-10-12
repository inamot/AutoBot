package se.kau.cs.jittac.eclipse.builders.jdt;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import se.kau.cs.jittac.model.im.IImplementationModelElement;
import se.kau.cs.jittac.model.im.IXReference;
import se.kau.cs.jittac.model.im.IXReferenceType;
import se.kau.cs.jittac.model.im.ImplementationModelFactory;
import se.kau.cs.jittac.model.im.ImplementationModelFactoryRegistry;
import se.kau.cs.jittac.model.im.ImplementationModelPartition;
import se.kau.cs.jittac.model.im.ImplementationModelPartitionType;

public class JDTImplementationModelFactory 
	implements ImplementationModelFactory
	<JDTJavaModelElement, IJavaElement, JDTJavaReference,JDTJavaReferenceCodeInformation> {

	public static final JDTImplementationModelFactory INSTANCE = new JDTImplementationModelFactory();
	private static final String SEPARATOR = "\t";
	private static final String SRC_START = "\"<SOURCE>\"";
	private static final String TRG_START = "\"<TARGET>\"";
	private static final String REF_START = "\"<REF>\"";
	private static final String INT_HEADER = "\"<INTERNAL>\"";
	private static final String EXT_HEADER = "\"<EXTERNAL>\"";
	
	private JDTImplementationModelFactory() {
	}

	
	public JDTJavaModelElement createElement(IJavaElement element, ImplementationModelPartition partition) {
		return new JDTJavaModelElement(element, partition);
	}
	
	@Override
	public JDTJavaReference createReference(JDTJavaModelElement src, JDTJavaModelElement trg, IXReferenceType type,
			JDTJavaReferenceCodeInformation internalRef) {
		
		return new JDTJavaReference(src, trg, type, internalRef);
	}

	@Override
	public IXReference<JDTJavaModelElement, JDTJavaReferenceCodeInformation> createExternalReference(
			JDTJavaModelElement src, IImplementationModelElement<?> trg, IXReferenceType type,
			JDTJavaReferenceCodeInformation internalRef) {
		throw new UnsupportedOperationException("Creation of external references not yet supported.");
	}

	@Override
	public ImplementationModelPartitionType getPartitionType() {
		return JDTJavaImplementationModelPartitionType.INSTANCE;
	}

	@Override
	public String serializeInternalReference(IXReference<?,?> reference) {
		Class<?> c = this.getPartitionType().getReferenceType();
		if (!reference.getClass().equals(c)) {
			throw new IllegalArgumentException("Can't serilialize reference " + reference);
		};
		JDTJavaReference jRef = (JDTJavaReference) reference;
		StringBuilder sb = new StringBuilder();
		sb.append(INT_HEADER);
		sb.append(SRC_START);
		sb.append(this.serializeLocalElement(jRef.getSource()));
		sb.append(TRG_START);
		sb.append(this.serializeLocalElement(jRef.getTarget()));
		sb.append(REF_START);
		sb.append(jRef.getType().name());
		sb.append(SEPARATOR);
		sb.append(jRef.getInternalReference().offset);
		sb.append(SEPARATOR);
		sb.append(jRef.getInternalReference().length);
		sb.append(SEPARATOR);
		sb.append(jRef.getInternalReference().line);
		return sb.toString();
	}

	@Override
	public String serializeExternalReference(IXReference<?,?> reference) {
		throw new UnsupportedOperationException("Creation of external references not yet supported.");
	}

	@Override
	public String serializeLocalElement(JDTJavaModelElement element) {
		StringBuilder sb = new StringBuilder();
		sb.append(element.getResource().getResourceModelName());
		sb.append(SEPARATOR);
		sb.append(element.getResource().getPersistentHandle());
		sb.append(SEPARATOR);
		sb.append(element.getElement().getHandleIdentifier());
		return sb.toString();
	}

	@Override
	public JDTJavaModelElement deserializeLocalElement(String elementAsString, ImplementationModelPartition partition) {
		String[] temp = elementAsString.split(SEPARATOR);
		if (temp.length != 3) {
			throw new IllegalArgumentException("Malformed element string.");
		}
		IJavaElement elem = JavaCore.create(temp[2]);
		if (elem == null) {
			throw new IllegalArgumentException("Malformed element string: cannot find Java element.");
		}
		return this.createElement(elem, partition);
	}

	@Override
	public JDTJavaReference deserializeInternalReference(String referenceAsString,
			ImplementationModelPartition partition) {
		String sourceStr, targetStr, referenceStr;
		String[] temp = referenceAsString.split(INT_HEADER, 2);
		if (temp.length != 2) {
			throw new IllegalArgumentException("String does not represent an internal reference.");
		}
		temp = temp[1].split(SRC_START, 2);
		if (temp.length != 2) {
			throw new IllegalArgumentException("Malformed reference string: cannot identify source element.");
		}
		temp = temp[1].split(TRG_START, 2);
		if (temp.length != 2) {
			throw new IllegalArgumentException("Malformed reference string: cannot identify target element.");
		}
		sourceStr = temp[0];
		temp = temp[1].split(REF_START, 2);
		if (temp.length != 2) {
			throw new IllegalArgumentException("Malformed reference string: cannot identify reference section.");
		}
		targetStr = temp[0];
		referenceStr = temp[1];
		
		JDTJavaModelElement src = deserializeLocalElement(sourceStr, partition);
		JDTJavaModelElement trg = deserializeLocalElement(targetStr, partition);
		
		temp = referenceStr.split(SEPARATOR);
		if (temp.length != 4) {
			throw new IllegalArgumentException("Malformed reference string: reference section malformed");
		}
		
		try {
			IXReferenceType type = IXReferenceType.valueOf(temp[0]);
			JDTJavaReferenceCodeInformation codeInfo =
					new JDTJavaReferenceCodeInformation(Integer.parseInt(temp[1]),
							Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
			return this.createReference(src, trg, type, codeInfo);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Malformed reference string: reference section malformed");
		}
	}

	@Override
	public IXReference<JDTJavaModelElement, ?> deserializeExternalReference(String referenceAsString,
			ImplementationModelPartition partition) {
		throw new UnsupportedOperationException("Creation of external references not yet supported.");
	}
}


