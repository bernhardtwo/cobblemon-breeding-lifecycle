package io.github.bernhardtwo.breedinglifecycle.mod;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server entrypoint of the Breeding Lifecycle mod. */
public final class BreedingLifecycleMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "breedinglifecycle";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        LOGGER.info("Breeding Lifecycle loaded");
    }
}
