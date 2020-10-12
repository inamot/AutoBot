package se.kau.cs.jittac.eclipse.ui.parts;

import com.google.inject.Provider;

import javafx.scene.paint.Color;

public class SelectionColorProvider implements Provider<Color> {

	@Override
	public Color get() {
		return Color.web("#00008866");
	}

}
