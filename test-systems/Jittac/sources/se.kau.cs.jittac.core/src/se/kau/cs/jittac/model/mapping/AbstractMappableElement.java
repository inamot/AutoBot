package se.kau.cs.jittac.model.mapping;

public abstract class AbstractMappableElement implements IMappableElement {

	@Override
	public boolean isDescendantOf(IMappableElement elem) {
		
		if (elem == null) return false;
		IMappableElement parent = this.getParent();
		if (parent != null) {
			if (parent.equals(elem)) {
				 return true;
			}
			else {
				return parent.isDescendantOf(elem);
			}
		}
		return false;
	}

}
