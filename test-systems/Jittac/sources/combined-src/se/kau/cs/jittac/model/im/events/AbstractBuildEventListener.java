package se.kau.cs.jittac.model.im.events;

public abstract class AbstractBuildEventListener implements BuildEventListener {

	@Override
	public void onBuildStart(BuildStartEvent event) {
	}

	@Override
	public void onBuildEnd(BuildEndEvent event) {
	}
	
	public void onBuildEvent(BuildEvent event) {
		if (event instanceof BuildStartEvent) {
			onBuildStart((BuildStartEvent) event);
		}
		else if (event instanceof BuildEndEvent) {
			onBuildEnd((BuildEndEvent) event);
		}
	}

}
