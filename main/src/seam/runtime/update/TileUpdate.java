package seam.runtime.update;

import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.world.*;
import seam.runtime.*;

public class TileUpdate extends RuntimeUpdate {
	public Block floor, overlay, block;
	public Team team;
	public int rotation;
	public Point2[] positions;

	public TileUpdate(@Nullable Block floor, @Nullable Block overlay, @Nullable Block block, Team team, int rotation, Point2... positions) {
		this.floor = floor;
		this.overlay = overlay;
		this.block = block;
		this.team = team;
		this.rotation = rotation;
		this.positions = positions;
	}

	@Override
	public void act(WorldRuntime stage) {
		World world = stage.world;

		world.setGenerating(true);

		for (Point2 pos : positions) {
			Tile tile = world.tile(pos.pack());

			if (tile == null) {
				Log.err("[FloorUpdate] Tile at (@, @) does not exist. Skipping");
				continue;
			}

			tile.setFloor(floor == null ? Blocks.air.asFloor() : floor.asFloor());
			tile.setOverlay(overlay == null ? Blocks.air.asFloor() : overlay);
			if (block == null) {
				tile.setBlock(Blocks.air);
			} else {
				tile.setBlock(block, team, rotation);
			}
			if (tile.build != null) {
				tile.build.updateProximity();
			} else {
				for (int i = 0; i < 4; i++) {
					Tile next = tile.nearby(i);

					if (next != null && next.build != null) next.build.updateProximity();
				}
			}
		}

		world.setGenerating(false);
	}
}
