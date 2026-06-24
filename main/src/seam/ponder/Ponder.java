package seam.ponder;

import arc.files.*;
import arc.util.serialization.*;
import mindustry.content.*;
import mindustry.core.*;
import seam.*;
import seam.core.*;
import seam.runtime.*;
import seam.runtime.update.*;

public class Ponder {
	public static SeamRuntime buildEmpty(String name, int width, int height) {
		SeamRuntime res = Seam.services.runtimes.create(
			SeamRuntimeConfig.builder().id(SeamRuntimeRegistry.nextId())
			.name(name)
			.size(width, height)
			.updatePolicy(SeamRuntimeUpdatePolicy.all())
			.build()
		);

		Seam.services.executor.call(res, SeamPhase.manual, runtime -> {
			generateFloor(runtime.world);
			return null;
		});

		return res;
	}

	public static void generateFloor(World world) {
		world.setGenerating(true);
		world.tiles.eachTile(tile -> {
			tile.setFloor(Blocks.metalTiles9.asFloor());

			if (tile.x % 4 > 0 && tile.y % 4 > 0) tile.setFloor(Blocks.metalTiles7.asFloor());
		});
		world.setGenerating(false);
	}

	public static SeamRuntime loadFromJson(Fi file) {
		if (!file.exists()) throw new RuntimeException("Cannot Parse runtime: File not found");

		JsonValue jsonFile = new JsonReader().parse(file);
		JsonValue updatePolicy = jsonFile.get("updatePolicy");

		int width = jsonFile.getInt("width", 1);
		int height = jsonFile.getInt("height", 1);

		SeamRuntime runtime = Seam.services.runtimes.create(
			SeamRuntimeConfig.builder()
				.name("ponder")
				.id(1)
				.size(width * 4 + 1, height * 4 + 1)
				.updatePolicy(
					updatePolicy == null ? SeamRuntimeUpdatePolicy.all() :
					SeamRuntimeUpdatePolicy.builder()
						.enabled(updatePolicy.getBoolean("updates", false))
						.buildings(updatePolicy.getBoolean("buildings", false))
						.bullets(updatePolicy.getBoolean("bullets", false))
						.collisions(updatePolicy.getBoolean("collisions", false))
						.draw(updatePolicy.getBoolean("draw", false))
						.fires(updatePolicy.getBoolean("fires", false))
						.power(updatePolicy.getBoolean("power", false))
						.puddles(updatePolicy.getBoolean("puddles", false))
						.sync(updatePolicy.getBoolean("sync", false))
						.teams(updatePolicy.getBoolean("teams", false))
						.units(updatePolicy.getBoolean("units", false))
						.weather(updatePolicy.getBoolean("weather", false))
						.build()
				)
				.build()
		);

		Seam.services.executor.call(runtime, SeamPhase.manual, run -> {
			generateFloor(run.world);
			return null;
		});

		return runtime;
	}
}
