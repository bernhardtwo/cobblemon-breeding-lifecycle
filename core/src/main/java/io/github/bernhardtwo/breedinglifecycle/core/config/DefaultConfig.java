package io.github.bernhardtwo.breedinglifecycle.core.config;

/** The config written when none exists. */
public final class DefaultConfig {
    private static final String JSON = """
            {
              "preset": "default",
              "presets": {
                "default": {
                  "p1": 0.33,
                  "r": 0.764,
                  "maxBreedings": null,
                  "ditto": { "p1": 0.33, "r": 0.764, "maxBreedings": null }
                }
              },
              "overrides": {},
              "referenceDaycare": { "enabled": false },
              "eggEventAdapter": { "enabled": false },
              "audit": { "enabled": true }
            }
            """;

    private DefaultConfig() {}

    /** Returns the default config as pretty-printed JSON. */
    public static String json() {
        return JSON;
    }
}
