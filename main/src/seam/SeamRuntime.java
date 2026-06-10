package seam;

import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;

public final class SeamRuntime{
    public final int id;
    public final String name;
    public final SeamRuntimeKind kind;

    public final World world;
    public final GameState state;
    public final SeamGroupSet groups;
    public final EntityCollisions collisions;

    private SeamRuntimeStatus status = SeamRuntimeStatus.created;

    private boolean updateEnabled = true;
    private boolean validateOnUpdate = true;

    public SeamRuntime(SeamRuntimeConfig config){
        config.validate();

        if(config.kind == SeamRuntimeKind.main){
            throw new IllegalArgumentException("Main runtime must be created with wrapCurrentMain().");
        }

        this.id = config.id;
        this.name = config.name;
        this.kind = config.kind;

        this.world = new World();
        this.state = new GameState();
        this.groups = new SeamGroupSet(config.width, config.height);
        this.collisions = new EntityCollisions();

        loadEmptyWorld(config.width, config.height);
    }

    public SeamRuntime(int id, String name, int width, int height){
        this(
        SeamRuntimeConfig.builder()
        .id(id)
        .name(name)
        .size(width, height)
        .kind(SeamRuntimeKind.subworld)
        .build()
        );
    }

    private SeamRuntime(
    int id,
    String name,
    SeamRuntimeKind kind,
    World world,
    GameState state,
    SeamGroupSet groups,
    EntityCollisions collisions
    ){
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.world = world;
        this.state = state;
        this.groups = groups;
        this.collisions = collisions;
        this.status = SeamRuntimeStatus.loaded;
        this.updateEnabled = kind != SeamRuntimeKind.main;
    }

    public static SeamRuntime wrapCurrentMain(){
        return new SeamRuntime(
        0,
        "main",
        SeamRuntimeKind.main,
        Vars.world,
        Vars.state,
        SeamGroupSet.wrapCurrent(),
        Vars.collisions
        );
    }

    private void loadEmptyWorld(int width, int height){
        setStatus(SeamRuntimeStatus.loading);

        world.resize(width, height);

        if(world.tiles != null){
            world.tiles.fill();
        }

        groups.resize(width, height);

        setStatus(SeamRuntimeStatus.loaded);
    }

    public SeamRuntimeStatus status(){
        return status;
    }

    public boolean disposed(){
        return status == SeamRuntimeStatus.disposed;
    }

    public boolean loaded(){
        return status == SeamRuntimeStatus.loaded;
    }

    public boolean main(){
        return kind == SeamRuntimeKind.main;
    }

    public boolean worldReady(){
        return SeamLifecycle.worldReady(world);
    }

    public boolean updateEnabled(){
        return updateEnabled && loaded() && !main();
    }

    public void updateEnabled(boolean updateEnabled){
        if(main() && updateEnabled){
            throw new IllegalStateException("Main runtime cannot be updated by SeamEngine.");
        }

        this.updateEnabled = updateEnabled;
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

        if(status != SeamRuntimeStatus.loaded){
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

        if(status == SeamRuntimeStatus.disposed){
            return;
        }

        groups.clear();
        status = SeamRuntimeStatus.disposed;
        updateEnabled = false;
    }

    private void setStatus(SeamRuntimeStatus status){
        this.status = status;
    }

    @Override
    public String toString(){
        return "SeamRuntime{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", kind=" + kind +
        ", status=" + status +
        '}';
    }
}