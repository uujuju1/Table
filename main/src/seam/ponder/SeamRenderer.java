package seam.ponder;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import seam.*;
import seam.graphics.*;
import seam.runtime.WorldRuntime;

public class SeamRenderer {
	public FetchBatch batch = new FetchBatch();

	private SeamFloorRenderer floorRenderer;

	public SeamRenderer() {
		Events.on(EventType.DisposeEvent.class, e -> {
			batch.dispose();
		});
		Events.on(EventType.ClientLoadEvent.class, e -> {
			if (!Vars.headless) {
				floorRenderer = new SeamFloorRenderer();
				floorRenderer.reference = batch;
			}
		});
	}

	public void drawRuntime(WorldRuntime runtime, float x, float y, float sclX, float sclY, float rot) {
		batch.x = x;
		batch.y = y;
		batch.scaleX = sclX;
		batch.scaleY = sclY;
		batch.rotation = rot;
		batch.begin();

		Seam.services.stack.enter(runtime);

		// TODO floors
		Draw.draw(Layer.floor, () -> floorRenderer.drawFloor());
		// TODO darkness
		// TODO overlay
		Draw.z(Layer.block);
		Vars.world.tiles.eachTile(tile -> {
			if (tile.build != null) tile.build.draw();
		});
		Groups.draw.each(Drawc::draw);

		Seam.services.stack.exit();

		batch.end();
	}
}
