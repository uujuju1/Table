package seam;

public enum SeamPhase{
    bootstrap,

    load,
    save,

    updatePre,
    updateBuildings,
    updatePower,
    updatePuddles,
    updateFires,
    updateWeather,
    updatePost,

    renderPrepare,
    renderWorld,
    renderEntities,
    renderDebug,

    input,
    networkRead,
    networkWrite,

    validate,
    manual
}