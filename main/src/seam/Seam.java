package seam;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.world.*;

public class Seam extends Mod{
    public static final SeamRuntimeStack stack = new SeamRuntimeStack();
    public static final SeamRuntimeRegistry runtimes = new SeamRuntimeRegistry();
    public static final SeamRuntimeExecutor executor = new SeamRuntimeExecutor(runtimes, stack);
    public static final SeamEngine engine = new SeamEngine(runtimes, stack, executor);
    public static final SeamConfigService config = new SeamConfigService(runtimes, executor);
    public static final SeamBuildService builds = new SeamBuildService(runtimes, executor);
    public static final SeamTerrainService terrain = new SeamTerrainService(runtimes, executor);
    public static final SeamQueryService query = new SeamQueryService(runtimes, executor);
    public static final SeamViewRegistry views = new SeamViewRegistry();
    public static final SeamPickService picks = new SeamPickService(runtimes, views, query);
    public static final SeamRenderService rendering = new SeamRenderService(runtimes, views);
    public static final SeamDrawScope drawScope = new SeamDrawScope(stack);
    public static final SeamWorldDraw worldDraw = new SeamWorldDraw(runtimes, views, rendering, drawScope);

    public static SeamRuntime mainRuntime;

    public Seam(){
        Log.info("[Seam] Loaded Seam constructor.");
    }

    @Override
    public void init(){
        SeamBootstrapValidator.validate();

        refreshMainRuntime();

        Events.on(WorldLoadEvent.class, event -> refreshMainRuntime());

        Events.on(ResetEvent.class, event -> {
            worldDraw.clear();
            rendering.clear();
            views.clear();
            runtimes.clearSubworlds();
            refreshMainRuntime();
        });

        Events.on(BlockDestroyEvent.class, event -> markActiveRuntimeTileDirty(event.tile));
        Events.on(TileChangeEvent.class, event -> markActiveRuntimeTileDirty(event.tile));

        Events.run(Trigger.afterGameUpdate, engine::update);
        Events.run(Trigger.draw, worldDraw::draw);

        Log.info("[Seam] Core initialized successfully.");
    }

    public static void refreshMainRuntime(){
        runtimes.refreshMain();
        mainRuntime = runtimes.main();
    }

    private static void markActiveRuntimeTileDirty(Tile tile){
        if(tile == null || !stack.active()){
            return;
        }

        SeamRuntime runtime = stack.current();

        if(runtime == null || runtime.main() || runtime.disposed() || !runtime.worldReady()){
            return;
        }

        if(tile.x < 0 || tile.y < 0 || tile.x >= runtime.world.width() || tile.y >= runtime.world.height()){
            return;
        }

        if(runtime.world.tile(tile.x, tile.y) != tile){
            return;
        }

        Block block = tile.block();
        int radius = Math.max(3, (block == null ? 1 : block.size) + 3);

        runtime.renderInvalidation.markAround(
        runtime,
        tile.x,
        tile.y,
        radius,
        SeamRenderInvalidationType.tile,
        SeamRenderInvalidationType.block,
        SeamRenderInvalidationType.light,
        SeamRenderInvalidationType.shadow,
        SeamRenderInvalidationType.proximity
        );
    }
}