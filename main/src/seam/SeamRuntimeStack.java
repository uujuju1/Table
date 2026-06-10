package seam;

import arc.struct.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;

public final class SeamRuntimeStack{
    private final Seq<Frame> stack = new Seq<>();

    public SeamRuntime current(){
        return stack.isEmpty() ? null : stack.peek().runtime;
    }

    public SeamPhase phase(){
        return stack.isEmpty() ? null : stack.peek().phase;
    }

    public boolean active(){
        return !stack.isEmpty();
    }

    public void enter(SeamRuntime runtime){
        enter(runtime, SeamPhase.manual);
    }

    public void enter(SeamRuntime runtime, SeamPhase phase){
        if(runtime == null){
            throw new NullPointerException("runtime");
        }

        if(phase == null){
            throw new NullPointerException("phase");
        }

        runtime.requireLoaded();

        ContextSnapshot snapshot = new ContextSnapshot();
        stack.add(new Frame(runtime, phase, snapshot));

        Vars.world = runtime.world;
        Vars.state = runtime.state;
        Vars.collisions = runtime.collisions;

        Groups.all = runtime.groups.all;
        Groups.player = runtime.groups.player;
        Groups.bullet = runtime.groups.bullet;
        Groups.unit = runtime.groups.unit;
        Groups.build = runtime.groups.build;
        Groups.sync = runtime.groups.sync;
        Groups.draw = runtime.groups.draw;
        Groups.fire = runtime.groups.fire;
        Groups.puddle = runtime.groups.puddle;
        Groups.weather = runtime.groups.weather;
        Groups.label = runtime.groups.label;
        Groups.powerGraph = runtime.groups.powerGraph;
    }

    public void exit(){
        if(stack.isEmpty()){
            throw new IllegalStateException("Cannot exit Seam runtime context: stack is empty.");
        }

        Frame frame = stack.pop();
        frame.snapshot.restore();
    }

    public void exitAll(){
        while(!stack.isEmpty()){
            exit();
        }
    }

    private static final class Frame{
        final SeamRuntime runtime;
        final SeamPhase phase;
        final ContextSnapshot snapshot;

        Frame(SeamRuntime runtime, SeamPhase phase, ContextSnapshot snapshot){
            this.runtime = runtime;
            this.phase = phase;
            this.snapshot = snapshot;
        }
    }

    private static final class ContextSnapshot{
        private final World world;
        private final GameState state;
        private final EntityCollisions collisions;

        private final EntityGroup<Entityc> all;
        private final EntityGroup<Player> player;
        private final EntityGroup<Bullet> bullet;
        private final EntityGroup<Unit> unit;
        private final EntityGroup<Building> build;
        private final EntityGroup<Syncc> sync;
        private final EntityGroup<Drawc> draw;
        private final EntityGroup<Fire> fire;
        private final EntityGroup<Puddle> puddle;
        private final EntityGroup<WeatherState> weather;
        private final EntityGroup<WorldLabel> label;
        private final EntityGroup<PowerGraphUpdaterc> powerGraph;

        ContextSnapshot(){
            this.world = Vars.world;
            this.state = Vars.state;
            this.collisions = Vars.collisions;

            this.all = Groups.all;
            this.player = Groups.player;
            this.bullet = Groups.bullet;
            this.unit = Groups.unit;
            this.build = Groups.build;
            this.sync = Groups.sync;
            this.draw = Groups.draw;
            this.fire = Groups.fire;
            this.puddle = Groups.puddle;
            this.weather = Groups.weather;
            this.label = Groups.label;
            this.powerGraph = Groups.powerGraph;
        }

        void restore(){
            Vars.world = world;
            Vars.state = state;
            Vars.collisions = collisions;

            Groups.all = all;
            Groups.player = player;
            Groups.bullet = bullet;
            Groups.unit = unit;
            Groups.build = build;
            Groups.sync = sync;
            Groups.draw = draw;
            Groups.fire = fire;
            Groups.puddle = puddle;
            Groups.weather = weather;
            Groups.label = label;
            Groups.powerGraph = powerGraph;
        }
    }
}