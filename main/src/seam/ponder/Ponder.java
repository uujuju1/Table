package seam.ponder;

import mindustry.content.*;
import seam.*;
import seam.core.*;
import seam.runtime.*;
import seam.runtime.update.*;

public class Ponder {

	public static SeamRuntime buildEmpty(String name) {
		SeamRuntime res = Seam.services.runtimes.create(
			SeamRuntimeConfig.builder().id(SeamRuntimeRegistry.nextId())
			.name(name)
			.size(5, 5)
			.updatePolicy(SeamRuntimeUpdatePolicy.all())
			.build()
		);

		Seam.services.executor.call(res, SeamPhase.manual, runtime -> {
			runtime.world.setGenerating(true);
			runtime.world.tiles.eachTile(tile -> tile.setFloor(Blocks.metalFloor.asFloor()));
			runtime.world.setGenerating(false);
			return null;
		});

		return res;
	}
}
