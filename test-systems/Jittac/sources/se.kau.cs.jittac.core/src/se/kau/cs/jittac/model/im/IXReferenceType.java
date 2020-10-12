package se.kau.cs.jittac.model.im;

/**
 * The different types of references supported in Jittac.
 * @author Sebastian Herold
 */
public enum IXReferenceType {
	UNKNOWN,
	ACCESS,
	ASSIGNMENT,
	CALL,
	CREATION,
	IMPORT,
	CONTAINMENT,
	TYPEREF;
}
