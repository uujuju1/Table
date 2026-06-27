package seam.runtime;

import arc.struct.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import seam.core.*;
import seam.entities.*;
import seam.runtime.control.*;
import seam.runtime.update.*;
import seam.world.*;

public final class WorldRuntime {
    public enum Kind{
        main,
        subworld
    }

    public enum Status{
        created,
        loading,
        loaded,
        disposed
    }

    public final int id;
    public final String name;
    public final Kind kind;

    public final World world;
    public final GameState state;
    public final SeamGroupSet groups;
    public final EntityCollisions collisions;
    public final SeamClock clock;
    public final Seq<RuntimeUpdate> updates = new Seq<>();

    private Status status = Status.created;

    private boolean validateOnUpdate = true;

    public WorldRuntime(SeamRuntimeConfig config){
        config.validate();

        if(config.kind == Kind.main){
            throw new IllegalArgumentException("Main runtime must be created with wrapCurrentMain().");
        }

        this.id = config.id;
        this.name = config.name;
        this.kind = config.kind;

        this.world = new SeamWorld();
        this.state = new GameState();
        this.groups = new SeamGroupSet(config.width, config.height);
        this.collisions = new EntityCollisions();
        this.clock = new SeamClock();

        loadEmptyWorld(config.width, config.height);
    }

    public WorldRuntime(int id, String name, int width, int height){
        this(
        SeamRuntimeConfig.builder()
        .id(id)
        .name(name)
        .size(width, height)
        .kind(Kind.subworld)
        .build()
        );
    }

    private WorldRuntime(
    int id,
    String name,
    Kind kind,
    World world,
    GameState state,
    SeamGroupSet groups,
    EntityCollisions collisions,
    SeamClock clock
    ){
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.world = world;
        this.state = state;
        this.groups = groups;
        this.collisions = collisions;
        this.clock = clock;
        this.status = Status.loaded;
    }

    public static WorldRuntime wrapCurrentMain(){
        return new WorldRuntime(
        0,
        "main",
        Kind.main,
        Vars.world,
        Vars.state,
        SeamGroupSet.wrapCurrent(),
        Vars.collisions,
        new SeamClock()
        );
    }

    private void loadEmptyWorld(int width, int height){
        setStatus(Status.loading);

        world.resize(width, height);

        if(world.tiles != null){
            world.tiles.fill();
        }

        groups.resize(width, height);
        clock.reset();

        setStatus(Status.loaded);
    }

    public Status status(){
        return status;
    }

    public boolean disposed(){
        return status == Status.disposed;
    }

    public boolean loaded(){
        return status == Status.loaded;
    }

    public boolean main(){
        return kind == Kind.main;
    }

    public boolean worldReady(){
        return SeamRuntimeValidator.worldReady(world);
    }

    public boolean updateEnabled(){
        return loaded() && !main();
    }

    public boolean validateOnUpdate(){
        return validateOnUpdate;
    }

    public void validateOnUpdate(boolean validateOnUpdate){
        this.validateOnUpdate = validateOnUpdate;
    }

    public void requireAlive(){
        if(disposed()){
            throw new IllegalStateException("Runtime '" + name + "' is disposed.");
        }
    }

    public void requireLoaded(){
        requireAlive();

        if(status != Status.loaded){
            throw new IllegalStateException("Runtime '" + name + "' is not loaded. Current status: " + status);
        }
    }

    public void requireWorldReady(){
        requireLoaded();

        if(!worldReady()){
            throw new IllegalStateException("Runtime '" + name + "' world is not ready.");
        }
    }

    public void dispose(){
        if(main()){
            throw new IllegalStateException("Main runtime cannot be disposed.");
        }

        if(status == Status.disposed){
            return;
        }

        groups.clear();
        clock.reset();
        status = Status.disposed;
    }

    private void setStatus(Status status){
        this.status = status;
    }

    @Override
    public String toString(){
        return "SeamRuntime{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", kind=" + kind +
        ", status=" + status +
        ", clock=" + clock +
        '}';
    }
}
