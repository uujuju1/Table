package seam.runtime.control;

public final class SeamRuntimeUpdatePolicy{
    public final boolean enabled;

    public final boolean teams;
    public final boolean buildings;
    public final boolean power;
    public final boolean puddles;
    public final boolean fires;
    public final boolean weather;
    public final boolean bullets;
    public final boolean units;
    public final boolean sync;
    public final boolean draw;
    public final boolean collisions;

    private SeamRuntimeUpdatePolicy(Builder builder){
        enabled = builder.enabled;

        teams = builder.teams;
        buildings = builder.buildings;
        power = builder.power;
        puddles = builder.puddles;
        fires = builder.fires;
        weather = builder.weather;
        bullets = builder.bullets;
        units = builder.units;
        sync = builder.sync;
        draw = builder.draw;
        collisions = builder.collisions;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static SeamRuntimeUpdatePolicy disabled(){
        return builder()
        .enabled(false)
        .build();
    }

    public static SeamRuntimeUpdatePolicy none(){
        return builder()
        .enabled(true)
        .build();
    }

    public static SeamRuntimeUpdatePolicy buildingsOnly(){
        return builder()
        .enabled(true)
        .teams(true)
        .buildings(true)
        .power(true)
        .build();
    }

    public static SeamRuntimeUpdatePolicy all(){
        return builder()
        .enabled(true)
        .teams(true)
        .buildings(true)
        .power(true)
        .puddles(true)
        .fires(true)
        .weather(true)
        .bullets(true)
        .units(true)
        .sync(true)
        .draw(true)
        .collisions(true)
        .build();
    }

    // TODO probably unnecessary to have a getter method if the value is public?
    public boolean enabled(){
        return enabled;
    }

    public boolean teams(){
        return teams;
    }

    public boolean buildings(){
        return buildings;
    }

    public boolean power(){
        return power;
    }

    public boolean puddles(){
        return puddles;
    }

    public boolean fires(){
        return fires;
    }

    public boolean weather(){
        return weather;
    }

    public boolean bullets(){
        return bullets;
    }

    public boolean units(){
        return units;
    }

    public boolean sync(){
        return sync;
    }

    public boolean draw(){
        return draw;
    }

    public boolean collisions(){
        return collisions;
    }

    public boolean updateTeams(){
        return enabled && teams;
    }

    public boolean updateBuildings(){
        return enabled && buildings;
    }

    public boolean updatePower(){
        return enabled && power;
    }

    public boolean updatePuddles(){
        return enabled && puddles;
    }

    public boolean updateFires(){
        return enabled && fires;
    }

    public boolean updateWeather(){
        return enabled && weather;
    }

    public boolean updateBullets(){
        return enabled && bullets;
    }

    public boolean updateUnits(){
        return enabled && units;
    }

    public boolean updateSync(){
        return enabled && sync;
    }

    public boolean updateDraw(){
        return enabled && draw;
    }

    public boolean updateCollisions(){
        return enabled && collisions;
    }

    public SeamRuntimeUpdatePolicy withEnabled(boolean enabled){
        return toBuilder()
        .enabled(enabled)
        .build();
    }

    public Builder toBuilder(){
        return builder()
        .enabled(enabled)
        .teams(teams)
        .buildings(buildings)
        .power(power)
        .puddles(puddles)
        .fires(fires)
        .weather(weather)
        .bullets(bullets)
        .units(units)
        .sync(sync)
        .draw(draw)
        .collisions(collisions);
    }

    @Override
    public String toString(){
        return "SeamRuntimeUpdatePolicy{" +
        "enabled=" + enabled +
        ", teams=" + teams +
        ", buildings=" + buildings +
        ", power=" + power +
        ", puddles=" + puddles +
        ", fires=" + fires +
        ", weather=" + weather +
        ", bullets=" + bullets +
        ", units=" + units +
        ", sync=" + sync +
        ", draw=" + draw +
        ", collisions=" + collisions +
        '}';
    }

    public static final class Builder{
        private boolean enabled = true;

        private boolean teams;
        private boolean buildings;
        private boolean power;
        private boolean puddles;
        private boolean fires;
        private boolean weather;
        private boolean bullets;
        private boolean units;
        private boolean sync;
        private boolean draw;
        private boolean collisions;

        private Builder(){
        }

        public Builder enabled(boolean enabled){
            this.enabled = enabled;
            return this;
        }

        public Builder teams(boolean teams){
            this.teams = teams;
            return this;
        }

        public Builder buildings(boolean buildings){
            this.buildings = buildings;
            return this;
        }

        public Builder power(boolean power){
            this.power = power;
            return this;
        }

        public Builder puddles(boolean puddles){
            this.puddles = puddles;
            return this;
        }

        public Builder fires(boolean fires){
            this.fires = fires;
            return this;
        }

        public Builder weather(boolean weather){
            this.weather = weather;
            return this;
        }

        public Builder bullets(boolean bullets){
            this.bullets = bullets;
            return this;
        }

        public Builder units(boolean units){
            this.units = units;
            return this;
        }

        public Builder sync(boolean sync){
            this.sync = sync;
            return this;
        }

        public Builder draw(boolean draw){
            this.draw = draw;
            return this;
        }

        public Builder collisions(boolean collisions){
            this.collisions = collisions;
            return this;
        }

        public SeamRuntimeUpdatePolicy build(){
            return new SeamRuntimeUpdatePolicy(this);
        }
    }
}