package seam.core;

import arc.*;
import mindustry.game.EventType.*;
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

        Events.run(Trigger.afterGameUpdate, engine::update);
    }

    public SeamRuntime mainRuntime(){
        return mainRuntime;
    }

    public void refreshMainRuntime(){
        runtimes.refreshMain();
        mainRuntime = runtimes.main();
    }
}
