package seam.ponder;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import seam.*;
import seam.graphics.*;
import seam.runtime.*;

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

	public void drawRuntime(SeamRuntime runtime, float x, float y, float sclX, float sclY, float rot) {
		batch.x = x;
		batch.y = y;
		batch.scaleX = sclX;
		batch.scaleY = sclY;
		batch.rotation = rot;
		batch.begin();

		Seam.services.stack.enter(runtime);

		Core.camera.position.sub(x, y);
		Core.camera.update();

		// TODO floors
		Draw.draw(Layer.floor, () -> floorRenderer.drawFloor());
		// TODO darkness
		// TODO overlay
		// TODO block (env)
		Groups.draw.each(Drawc::draw);

		Core.camera.position.add(x, y);
		Core.camera.update();

		Seam.services.stack.exit();

		batch.end();
	}
}
