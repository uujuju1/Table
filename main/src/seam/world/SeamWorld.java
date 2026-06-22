package seam.world;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.game.*;
import seam.runtime.*;

@SuppressWarnings("unchecked")
public class SeamWorld extends World {
	public SeamRuntime runtime;

	public SeamWorld() {
		ObjectMap<Object, Seq<Cons<?>>> events = Reflect.get(Events.class, "events");
		Events.remove(EventType.TileChangeEvent.class, (Cons<EventType.TileChangeEvent>) events.get(EventType.TileChangeEvent.class, () -> Seq.with(new Cons[]{e -> {}})).first());
		Events.remove(EventType.TileFloorChangeEvent.class, (Cons<EventType.TileFloorChangeEvent>) events.get(EventType.TileFloorChangeEvent.class, () -> Seq.with(new Cons[]{e -> {}})).first());
		Events.remove(EventType.WorldLoadEvent.class, (Cons<EventType.WorldLoadEvent>) events.get(EventType.WorldLoadEvent.class, () -> Seq.with(new Cons[]{e -> {}})).first());
	}
}
