package io.github.bernhardtwo.breedinglifecycle.mod;

import io.github.bernhardtwo.breedinglifecycle.mod.adapter.EggEventAdapter;
import io.github.bernhardtwo.breedinglifecycle.mod.audit.AuditLog;
import io.github.bernhardtwo.breedinglifecycle.mod.command.BreedingLifecycleCommand;
import io.github.bernhardtwo.breedinglifecycle.mod.config.ConfigHolder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server entrypoint of the Breeding Lifecycle mod. */
public final class BreedingLifecycleMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "breedinglifecycle";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        var config = ConfigHolder.open(FabricLoader.getInstance().getConfigDir().resolve("breeding-lifecycle.json"));
        var audit = new AuditLog(
                FabricLoader.getInstance().getGameDir().resolve("logs/breeding-lifecycle/audit.jsonl"),
                () -> config.current().audit());
        var service = new LifecycleService(config, audit);
        LifecycleService.install(service);
        ServerLifecycleEvents.SERVER_STARTED.register(service::attach);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> audit.close());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            var errors = config.reload();
            if (!errors.isEmpty()) {
                LOGGER.error("Config not reloaded, keeping the previous one: {}", String.join("; ", errors));
            }
        });
        EggEventAdapter.register();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registries, environment) -> BreedingLifecycleCommand.register(dispatcher));
        LOGGER.info("Breeding Lifecycle loaded");
    }
}
