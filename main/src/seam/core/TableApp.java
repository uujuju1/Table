package seam.core;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import mindustry.game.EventType.*;
import seam.ponder.*;
import seam.runtime.*;
import seam.runtime.WorldRuntime;
import seam.runtime.control.*;

public class TableApp implements ApplicationListener {
	public final SeamRuntimeStack stack = new SeamRuntimeStack();
	public final SeamRuntimeRegistry runtimes = new SeamRuntimeRegistry();
	public final SeamRuntimeExecutor executor = new SeamRuntimeExecutor(runtimes, stack);
	public final RuntimeLogic engine = new RuntimeLogic(runtimes, stack, executor);

	public final RuntimeRenderer renderer = new RuntimeRenderer();

	private final ScreenQuad screenQuad = new ScreenQuad();

	private WorldRuntime mainRuntime;
	private boolean eventsInstalled;

	@Override
	public void dispose() {
		screenQuad.dispose();
	}

	public void init(Runnable refresh) {
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

		if (Core.input.keyDown(KeyCode.l)) draw();
	}

	public void draw() {
		Core.graphics.clear(Color.black);
		drawBackground();
		drawRuntimes();
		drawUI();
	}
	public void drawBackground() {
		// TODO shader
//		screenQuad.render(TableShaders.background);
	}
	public void drawRuntimes() {

	}
	public void drawUI() {

	}
}
