package seam.core;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import seam.runtime.*;
import seam.runtime.control.*;
import seam.runtime.update.*;

public final class SeamEngine {
	private final SeamRuntimeRegistry runtimes;
	private final SeamRuntimeStack stack;
	private final SeamRuntimeExecutor executor;

	private boolean enabled = true;
	private boolean automatic = true;
	private boolean validateAfterStep = true;
	private boolean respectMainPause = true;

	public SeamEngine(SeamRuntimeRegistry runtimes, SeamRuntimeStack stack, SeamRuntimeExecutor executor) {
		if (runtimes == null) throw new NullPointerException("runtimes");
		if (stack == null) throw new NullPointerException("stack");
		if (executor == null) throw new NullPointerException("executor");

		this.runtimes = runtimes;
		this.stack = stack;
		this.executor = executor;
	}

	public boolean enabled() {
		return enabled;
	}

	public void enabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean automatic() {
		return automatic;
	}

	public void automatic(boolean automatic) {
		this.automatic = automatic;
	}

	public boolean validateAfterStep() {
		return validateAfterStep;
	}

	public void validateAfterStep(boolean validateAfterStep) {
		this.validateAfterStep = validateAfterStep;
	}

	public boolean respectMainPause() {
		return respectMainPause;
	}

	public void respectMainPause(boolean respectMainPause) {
		this.respectMainPause = respectMainPause;
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

		Seq<SeamRuntime> copy = runtimes.all();

		for (SeamRuntime runtime : copy) {
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

	private void updateRuntime(SeamRuntime runtime) {
		runtime.requireWorldReady();

		if (runtime.validateOnUpdate()) {
			SeamRuntimeValidator.validateRuntime(runtime, false);
		}

		SeamRuntimeUpdatePolicy policy = runtime.updatePolicy();

		run(runtime, SeamPhase.updatePre, active -> {
			active.clock.advance(Time.delta);
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
			if (active.validateOnUpdate()) {
				SeamRuntimeValidator.validateActiveContext(active);
			}

			return null;
		});
	}

	private void updateLightweightBuildingRuntime(SeamRuntime runtime, SeamRuntimeUpdatePolicy policy) {
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
		SeamRuntime runtime,
		SeamPhase phase,
		SeamRuntimeExecutor.Call<Void> action
	) {
		executor.call(runtime, phase, active -> {
			action.run(active);
			return null;
		});
	}
}
