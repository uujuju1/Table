package seam;

import arc.util.*;
import mindustry.mod.*;
import seam.core.*;
import seam.runtime.*;

public class Seam extends Mod{
    public static final SeamServices services = new SeamServices();

    public static SeamRuntime mainRuntime;

    public Seam(){
        Log.info("[Seam] Loaded Seam constructor.");
    }

    @Override
    public void init(){
        SeamBootstrapValidator.validate();

        refreshMainRuntime();
        services.installEvents(Seam::refreshMainRuntime);

        Log.info("[Seam] Core initialized successfully.");
    }

    public static void refreshMainRuntime(){
        services.refreshMainRuntime();
        mainRuntime = services.mainRuntime();
    }
}
