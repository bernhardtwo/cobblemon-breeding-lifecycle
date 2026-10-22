package io.github.bernhardtwo.breedinglifecycle.screenshots;

import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

/** Development-only client entrypoint; does nothing unless {@code -Dbreedinglifecycle.screenshots=true}. */
public final class ScreenshotsClient implements ClientModInitializer {
    private static final String OUTPUT = "breedinglifecycle.screenshots.output";

    @Override
    public void onInitializeClient() {
        Optional<Path> serverConfig = ScreenshotsSwitch.read(System.getProperties());
        if (serverConfig.isEmpty()) {
            return;
        }
        Path output = Path.of(System.getProperty(OUTPUT, "../../docs/screenshots"));
        SceneRunner runner = new SceneRunner(Minecraft.getInstance(), serverConfig.get(), output);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            runner.tick();
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                runner.onMessage(message.getString());
            }
        });
    }
}
