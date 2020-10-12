package se.kau.cs.jittac.model.im.events;

import se.kau.cs.jittac.model.im.IImplementationModel;

public interface IImplementationChangeListener {

	void onXReferenceDeltaEvent(XReferenceChangeDeltaEvent event);
	void onCompleteLoad(IImplementationModel im);
}
