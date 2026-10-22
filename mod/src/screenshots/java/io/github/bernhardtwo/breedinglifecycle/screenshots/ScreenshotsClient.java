package io.github.bernhardtwo.breedinglifecycle.screenshots;

import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;

/** Development-only client entrypoint; does nothing unless {@code -Dbreedinglifecycle.screenshots=true}. */
public final class ScreenshotsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Optional<Path> serverConfig = ScreenshotsSwitch.read(System.getProperties());
        if (serverConfig.isEmpty()) {
            return;
        }
    }
}
