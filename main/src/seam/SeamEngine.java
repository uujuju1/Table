package seam;

import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

public final class SeamEngine{
    private final SeamRuntimeRegistry runtimes;
    private final SeamRuntimeStack stack;

    private boolean enabled = true;
    private boolean automatic = true;
    private boolean validateAfterStep = true;

    public SeamEngine(SeamRuntimeRegistry runtimes, SeamRuntimeStack stack){
        if(runtimes == null){
            throw new NullPointerException("runtimes");
        }

        if(stack == null){
            throw new NullPointerException("stack");
        }

        this.runtimes = runtimes;
        this.stack = stack;
    }

    public boolean enabled(){
        return enabled;
    }

    public void enabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean automatic(){
        return automatic;
    }

    public void automatic(boolean automatic){
        this.automatic = automatic;
    }

    public boolean validateAfterStep(){
        return validateAfterStep;
    }

    public void validateAfterStep(boolean validateAfterStep){
        this.validateAfterStep = validateAfterStep;
    }

    public void update(){
        if(!automatic){
            return;
        }

        if(!SeamLifecycle.mainWorldReady()){
            return;
        }

        step();
    }

    public void step(){
        if(!enabled){
            return;
        }

        if(!SeamLifecycle.mainWorldReady()){
            throw new IllegalStateException("Cannot step SeamEngine: main world is not ready.");
        }

        if(stack.active()){
            throw new IllegalStateException("Cannot step SeamEngine while a runtime context is already active.");
        }

        Seq<SeamRuntime> copy = runtimes.all();

        for(SeamRuntime runtime : copy){
            if(!runtime.updateEnabled()){
                continue;
            }

            updateRuntime(runtime);
        }

        if(validateAfterStep){
            SeamRuntimeValidator.validateRestoredToMain(runtimes, stack);
        }
    }

    public void step(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("Step amount cannot be negative.");
        }

        for(int i = 0; i < amount; i++){
            step();
        }
    }

    private void updateRuntime(SeamRuntime runtime){
        runtime.requireWorldReady();

        if(runtime.validateOnUpdate()){
            SeamRuntimeValidator.validateRuntime(runtime, false);
        }

        run(runtime, SeamPhase.updatePre, () -> {
            runtime.state.tick++;
        });

        run(runtime, SeamPhase.updateBuildings, () -> {
            Groups.build.update();
        });

        run(runtime, SeamPhase.updatePower, () -> {
            Groups.powerGraph.update();
        });

        run(runtime, SeamPhase.updatePuddles, () -> {
            Groups.puddle.update();
        });

        run(runtime, SeamPhase.updateFires, () -> {
            Groups.fire.update();
        });

        run(runtime, SeamPhase.updateWeather, () -> {
            Groups.weather.update();
        });

        run(runtime, SeamPhase.updatePost, () -> {
            if(runtime.validateOnUpdate()){
                SeamRuntimeValidator.validateActiveContext(runtime);
            }
        });
    }

    private void run(SeamRuntime runtime, SeamPhase phase, Runnable action){
        stack.enter(runtime, phase);

        try{
            if(runtime.validateOnUpdate()){
                SeamRuntimeValidator.validateActiveContext(runtime);
            }

            action.run();
        }catch(Throwable throwable){
            Log.err("Seam runtime update failed. Runtime: @, phase: @", runtime, phase);
            throw throwable;
        }finally{
            stack.exit();
        }
    }
}