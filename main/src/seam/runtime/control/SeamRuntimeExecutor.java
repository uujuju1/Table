package seam.runtime.control;

import seam.runtime.*;

public final class SeamRuntimeExecutor {
	public interface Call<T> {
		T run(WorldRuntime runtime);
	}

	private final SeamRuntimeRegistry runtimes;
	private final SeamRuntimeStack stack;

	public SeamRuntimeExecutor(SeamRuntimeRegistry runtimes, SeamRuntimeStack stack) {
		this.runtimes = runtimes;
		this.stack = stack;
	}

	public <T> T call(WorldRuntime runtime, Call<T> task) {
		runtime.requireWorldReady();

		stack.enter(runtime);

		try {
			return task.run(runtime);
		} finally {
			stack.exit();
		}
	}
}
