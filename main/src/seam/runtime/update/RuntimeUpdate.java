package seam.runtime.update;

import seam.runtime.*;

public abstract class RuntimeUpdate {
	public float startTime, endTime;

	public boolean acted;

	public void act(WorldRuntime stage) {
	}

	public void update(WorldRuntime stage) {

	}
}
