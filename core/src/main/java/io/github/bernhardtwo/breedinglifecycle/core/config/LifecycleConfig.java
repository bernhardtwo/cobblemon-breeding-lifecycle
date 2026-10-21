package io.github.bernhardtwo.breedinglifecycle.core.config;

import com.google.gson.JsonObject;
import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import java.util.Map;

/**
 * A validated config: the selected preset, every preset, the effective curves after overrides, and the raw JSON.
 */
public record LifecycleConfig(
        String preset, Map<String, BreedingCurves> presets, BreedingCurves effective, JsonObject raw) {
    public LifecycleConfig {
        presets = Map.copyOf(presets);
        raw = raw.deepCopy();
    }

    @Override
    public JsonObject raw() {
        return raw.deepCopy();
    }
}
