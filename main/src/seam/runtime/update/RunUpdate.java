package seam.runtime.update;

import arc.func.*;
import seam.runtime.*;

public class RunUpdate extends RuntimeUpdate {
	public Cons<WorldRuntime> actRun, updateRun;

	public RunUpdate(Cons<WorldRuntime> actRun, Cons<WorldRuntime> updateRun) {
		this.actRun = actRun;
		this.updateRun = updateRun;
	}

	@Override
	public void update(WorldRuntime stage) {
		updateRun.get(stage);
	}
}
