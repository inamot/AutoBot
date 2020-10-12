package se.kau.cs.jittac.eclipse.ui.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import se.kau.cs.jittac.eclipse.ui.parts.ComponentPart;

public class ItemCreationModel {

	public static enum Type {
		None,
		Component,
		Connector
	};
	
	private ObjectProperty<Type> typeProperty = new SimpleObjectProperty<ItemCreationModel.Type>(Type.None);
	private ObjectProperty<ComponentPart> srcProperty = new SimpleObjectProperty<ComponentPart>();

	public ObjectProperty<Type> getTypeProperty() {
		return typeProperty;
	}

	public Type getType() {
		return typeProperty.getValue();
	}

	public void setType(Type type) {
		this.typeProperty.setValue(type);
	}
	
	public ComponentPart getSource() {
		return srcProperty.getValue();
	}
	
	public ObjectProperty<ComponentPart> getSourceProperty() {
		return srcProperty;
	}

	public void setSource(ComponentPart part) {
		srcProperty.setValue(part);
	}
}
