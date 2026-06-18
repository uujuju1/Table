package seam.ponder;

import seam.*;
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

//		World old = Vars.world;
//		Vars.world = res.world;

//		res.world.setGenerating(true);
//
//		res.world.tiles.eachTile(t -> t.setFloor(Blocks.metalFloor.asFloor()));
//
//		res.world.tiles.get(5, 5).setBlock(Blocks.copperWall, Team.sharded);
//
//		res.world.setGenerating(false);
//
//		Vars.world = old;

//		SeamTileMutator.fillFloor(res, Blocks.grass.asFloor());
//		SeamTileMutator.place(res, 5, 5, Blocks.conveyor, Team.sharded, 0);
//		Seam.builds.place(res, 5, 5, Blocks.conveyor, Team.sharded, 0);

		return res;
	}
}
