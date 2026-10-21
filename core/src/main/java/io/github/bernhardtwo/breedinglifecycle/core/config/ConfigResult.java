package io.github.bernhardtwo.breedinglifecycle.core.config;

import java.util.List;

/** Outcome of parsing a config: either a usable config or every error found. */
public sealed interface ConfigResult permits ConfigResult.Valid, ConfigResult.Invalid {
    /** A config that passed validation. */
    record Valid(LifecycleConfig config) implements ConfigResult {}

    /** A rejected config; each error names the field path, the value and the allowed range. */
    record Invalid(List<String> errors) implements ConfigResult {
        public Invalid {
            errors = List.copyOf(errors);
        }
    }
}
