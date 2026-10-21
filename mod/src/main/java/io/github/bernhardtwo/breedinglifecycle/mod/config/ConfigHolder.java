package io.github.bernhardtwo.breedinglifecycle.mod.config;

import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The active settings; a reload swaps them only when the new file is valid. */
public final class ConfigHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger("breedinglifecycle");

    private final Path file;
    private volatile ModSettings current;

    private ConfigHolder(Path file, ModSettings current) {
        this.file = file;
        this.current = current;
    }

    /** Loads {@code file}; when it is invalid, logs the errors and starts from the defaults. */
    public static ConfigHolder open(Path file) {
        return switch (ModConfig.load(file)) {
            case ModConfig.Result.Loaded loaded -> {
                if (loaded.created()) {
                    LOGGER.info("Wrote default config to {}", file);
                }
                yield new ConfigHolder(file, loaded.settings());
            }
            case ModConfig.Result.Rejected rejected -> {
                LOGGER.error("Invalid config {}, using defaults: {}", file, String.join("; ", rejected.errors()));
                yield new ConfigHolder(file, ModConfig.defaults());
            }
        };
    }

    /** The settings in use. */
    public ModSettings current() {
        return current;
    }

    /** Re-reads the file; returns no errors when the settings changed, or the errors that kept the old ones. */
    public List<String> reload() {
        return switch (ModConfig.load(file)) {
            case ModConfig.Result.Loaded loaded -> {
                current = loaded.settings();
                yield List.of();
            }
            case ModConfig.Result.Rejected rejected -> rejected.errors();
        };
    }
}
