package seam.core;

import arc.*;
import mindustry.game.EventType.*;
import mindustry.world.*;
import seam.graphics.invalidation.*;
import seam.ponder.*;
import seam.runtime.*;
import seam.runtime.control.*;

public final class SeamServices{
    public final SeamRuntimeStack stack = new SeamRuntimeStack();
    public final SeamRuntimeRegistry runtimes = new SeamRuntimeRegistry();
    public final SeamRuntimeExecutor executor = new SeamRuntimeExecutor(runtimes, stack);
    public final SeamEngine engine = new SeamEngine(runtimes, stack, executor);

    public final SeamRenderer renderer = new SeamRenderer();

    private SeamRuntime mainRuntime;
    private boolean eventsInstalled;

    public void clearForReset(){
        runtimes.clearSubworlds();
    }

    public void init(Runnable refresh) {
        SeamBootstrapValidator.validate();

        refreshMainRuntime();
        installEvents(refresh);
    }

    public void installEvents(Runnable refreshMainRuntime){
        if(refreshMainRuntime == null){
            throw new NullPointerException("refreshMainRuntime");
        }

        if(eventsInstalled){
            return;
        }

        eventsInstalled = true;

        Events.on(WorldLoadEvent.class, event -> refreshMainRuntime.run());

        Events.on(ResetEvent.class, event -> {
            clearForReset();
            refreshMainRuntime.run();
        });

        Events.on(BlockDestroyEvent.class, event -> markActiveRuntimeTileDirty(event.tile));
        Events.on(TileChangeEvent.class, event -> markActiveRuntimeTileDirty(event.tile));

        Events.run(Trigger.afterGameUpdate, engine::update);
    }

    public SeamRuntime mainRuntime(){
        return mainRuntime;
    }

    private void markActiveRuntimeTileDirty(Tile tile){
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

    public void refreshMainRuntime(){
        runtimes.refreshMain();
        mainRuntime = runtimes.main();
    }
}
