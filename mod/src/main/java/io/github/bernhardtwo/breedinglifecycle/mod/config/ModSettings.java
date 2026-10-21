package io.github.bernhardtwo.breedinglifecycle.mod.config;

import io.github.bernhardtwo.breedinglifecycle.core.config.LifecycleConfig;
import java.util.Objects;

/** The validated curves plus the mod-only switches of {@code breeding-lifecycle.json}. */
public record ModSettings(LifecycleConfig lifecycle, boolean referenceDaycare, boolean eggEventAdapter, boolean audit) {
    public ModSettings {
        Objects.requireNonNull(lifecycle, "lifecycle");
    }
}
