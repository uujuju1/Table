package seam.core;

import arc.*;
import mindustry.game.EventType.*;
import seam.ponder.*;
import seam.runtime.*;
import seam.runtime.WorldRuntime;
import seam.runtime.control.*;

public final class SeamServices implements ApplicationListener {
	public final SeamRuntimeStack stack = new SeamRuntimeStack();
	public final SeamRuntimeRegistry runtimes = new SeamRuntimeRegistry();
	public final SeamRuntimeExecutor executor = new SeamRuntimeExecutor(runtimes, stack);
	public final SeamEngine engine = new SeamEngine(runtimes, stack, executor);

	public final SeamRenderer renderer = new SeamRenderer();

	private WorldRuntime mainRuntime;
	private boolean eventsInstalled;

	public void init(Runnable refresh) {
		SeamBootstrapValidator.validate();

		refreshMainRuntime();
		installEvents(refresh);

		Core.app.addListener(this);
	}

	public void installEvents(Runnable refreshMainRuntime) {
		if (eventsInstalled) {
			return;
		}

		eventsInstalled = true;

		Events.on(WorldLoadEvent.class, event -> refreshMainRuntime.run());

		Events.on(ResetEvent.class, event -> {
			runtimes.clearSubworlds();
			refreshMainRuntime.run();
		});
	}

	public WorldRuntime mainRuntime() {
		return mainRuntime;
	}

	public void refreshMainRuntime() {
		runtimes.refreshMain();
		mainRuntime = runtimes.main();
	}

	@Override
	public void update() {
		engine.update();
	}
}
