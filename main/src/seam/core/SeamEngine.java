package seam.core;

import arc.struct.*;
import mindustry.*;
import mindustry.gen.*;
import seam.runtime.*;
import seam.runtime.control.*;

public final class SeamEngine {
	private final SeamRuntimeRegistry runtimes;
	private final SeamRuntimeStack stack;
	private final SeamRuntimeExecutor executor;

	public boolean enabled = true;
	public boolean automatic = true;
	public boolean validateAfterStep = true;
	public boolean respectMainPause = true;

	public SeamEngine(SeamRuntimeRegistry runtimes, SeamRuntimeStack stack, SeamRuntimeExecutor executor) {
		this.runtimes = runtimes;
		this.stack = stack;
		this.executor = executor;
	}

	public void update() {
		if (!automatic) return;
		if (!SeamRuntimeValidator.mainWorldReady()) return;
		if (respectMainPause && Vars.state != null && Vars.state.isPaused()) return;

		step();
	}

	public void step() {
		if (!enabled) return;

		if (!SeamRuntimeValidator.mainWorldReady()) throw new IllegalStateException("Cannot step SeamEngine: main world is not ready.");
		if (stack.active()) throw new IllegalStateException("Cannot step SeamEngine while a runtime context is already active.");

		Seq<WorldRuntime> copy = runtimes.all();

		for (WorldRuntime runtime : copy) {
			if (!runtime.updateEnabled()) continue;

			updateRuntime(runtime);
		}

		if (validateAfterStep) {
			SeamRuntimeValidator.validateRestoredToMain(runtimes, stack);
		}
	}

	public void step(int amount) {
		if (amount < 0) throw new IllegalArgumentException("Step amount cannot be negative.");

		for (int i = 0; i < amount; i++) step();
	}

	private void updateRuntime(WorldRuntime runtime) {
		runtime.requireWorldReady();

		if (runtime.validateOnUpdate()) {
			SeamRuntimeValidator.validateRuntime(runtime, false);
		}

		SeamRuntimeUpdatePolicy policy = runtime.updatePolicy();

		run(runtime, SeamPhase.updatePre, active -> {
			active.clock.advance();
			active.state.tick += active.clock.delta();
			active.state.updateId++;
			return null;
		});

		if (policy.teams) {
			run(runtime, SeamPhase.updateTeams, active -> {
				active.state.teams.updateTeamStats();
				return null;
			});
		}

		if (usesVanillaCentralEntityUpdate(policy)) {
			run(runtime, SeamPhase.updateGroups, active -> {
				Groups.update();
				return null;
			});
		} else {
			updateLightweightBuildingRuntime(runtime, policy);
		}

		run(runtime, SeamPhase.updatePost, active -> {
			active.updates.each(update -> active.clock.time() > update.startTime && active.clock.time() < update.endTime || update.startTime == update.endTime, update -> {
				if (!update.acted) update.act(active);
				update.acted = true;
				update.update(active);
			});

			if (active.validateOnUpdate()) SeamRuntimeValidator.validateActiveContext(active);

			return null;
		});
	}

	private void updateLightweightBuildingRuntime(WorldRuntime runtime, SeamRuntimeUpdatePolicy policy) {
		if (policy.buildings) {
			run(runtime, SeamPhase.updateBuildings, active -> {
				Groups.build.update();
				return null;
			});
		}

		if (policy.power) {
			run(runtime, SeamPhase.updatePower, active -> {
				Groups.powerGraph.update();
				return null;
			});
		}
	}

	private boolean usesVanillaCentralEntityUpdate(SeamRuntimeUpdatePolicy policy) {
		return policy.puddles
			|| policy.fires
			|| policy.weather
			|| policy.bullets
			|| policy.units
			|| policy.sync
			|| policy.draw
			|| policy.collisions;
	}

	private void run(
		WorldRuntime runtime,
		SeamPhase phase,
		SeamRuntimeExecutor.Call<Void> action
	) {
		executor.call(runtime, phase, active -> {
			action.run(active);
			return null;
		});
	}
}
