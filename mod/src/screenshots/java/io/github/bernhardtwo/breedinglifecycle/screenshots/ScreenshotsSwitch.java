package io.github.bernhardtwo.breedinglifecycle.screenshots;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/** Reads whether the dev client should take the in-game screenshots, and where the server config lives. */
public final class ScreenshotsSwitch {
    static final String ENABLED = "breedinglifecycle.screenshots";
    static final String SERVER_CONFIG = "breedinglifecycle.screenshots.serverConfig";

    private ScreenshotsSwitch() {}

    /** Empty unless the switch is exactly {@code true}; then the server config path, which must be set. */
    public static Optional<Path> read(Properties props) {
        if (!"true".equals(props.getProperty(ENABLED))) {
            return Optional.empty();
        }
        String config = props.getProperty(SERVER_CONFIG);
        if (config == null || config.isBlank()) {
            throw new IllegalStateException(SERVER_CONFIG + " is not set");
        }
        return Optional.of(Path.of(config));
    }
}
