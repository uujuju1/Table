package seam.runtime;

import seam.runtime.control.*;
import arc.struct.*;

public final class SeamRuntimeRegistry{
    private final IntMap<WorldRuntime> byId = new IntMap<>();
    private final Seq<WorldRuntime> runtimes = new Seq<>();
    public static int curId;

    private WorldRuntime main;

    public WorldRuntime main(){
        return main;
    }

    public Seq<WorldRuntime> all(){
        return runtimes.copy();
    }

    public WorldRuntime get(int id){
        return byId.get(id);
    }

    public boolean contains(int id){
        return byId.containsKey(id);
    }

    public static int nextId() {
        curId++;
        return curId;
    }

    public void refreshMain(){
        WorldRuntime refreshedMain = WorldRuntime.wrapCurrentMain();

        SeamRuntimeValidator.validateRuntime(refreshedMain, false);

        if(main != null){
            byId.remove(main.id);
            runtimes.remove(main, true);
        }

        main = refreshedMain;

        byId.put(main.id, main);
        runtimes.add(main);

        SeamRuntimeValidator.validateRegistry(this);
    }

    public WorldRuntime create(SeamRuntimeConfig config){
        config.validate();

        if(config.kind == WorldRuntime.Kind.main){
            throw new IllegalArgumentException("Main runtime is managed by refreshMain().");
        }

        if(config.id == 0){
            throw new IllegalArgumentException("Runtime id 0 is reserved for main runtime.");
        }

        if(byId.containsKey(config.id)){
            throw new IllegalArgumentException("Runtime id already exists: " + config.id);
        }

        WorldRuntime runtime = new WorldRuntime(config);

        SeamRuntimeValidator.validateRuntime(runtime, true);

        byId.put(runtime.id, runtime);
        runtimes.add(runtime);

        SeamRuntimeValidator.validateRegistry(this);

        return runtime;
    }

    public void remove(int id){
        WorldRuntime runtime = byId.get(id);

        if(runtime == null){
            return;
        }

        if(runtime.main()){
            throw new IllegalStateException("Main runtime cannot be removed.");
        }

        byId.remove(id);
        runtimes.remove(runtime, true);

        runtime.dispose();

        SeamRuntimeValidator.validateRegistry(this);
    }

    public void clearSubworlds(){
        Seq<WorldRuntime> copy = runtimes.copy();

        for(WorldRuntime runtime : copy){
            if(!runtime.main()){
                remove(runtime.id);
            }
        }

        SeamRuntimeValidator.validateRegistry(this);
    }
}
