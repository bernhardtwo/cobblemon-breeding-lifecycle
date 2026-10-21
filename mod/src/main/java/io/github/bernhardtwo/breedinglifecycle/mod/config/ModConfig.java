package io.github.bernhardtwo.breedinglifecycle.mod.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.bernhardtwo.breedinglifecycle.core.config.ConfigParser;
import io.github.bernhardtwo.breedinglifecycle.core.config.ConfigResult;
import io.github.bernhardtwo.breedinglifecycle.core.config.DefaultConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Reads {@code breeding-lifecycle.json}, writing the default first when it is missing. */
public final class ModConfig {
    private ModConfig() {}

    /** Outcome of loading the file. */
    public sealed interface Result permits Result.Loaded, Result.Rejected {
        /** A valid file; {@code created} is true when the default had to be written first. */
        record Loaded(ModSettings settings, boolean created) implements Result {}

        /** An invalid or unreadable file, with every error found. */
        record Rejected(List<String> errors) implements Result {
            public Rejected {
                errors = List.copyOf(errors);
            }
        }
    }

    /** Loads {@code file}; never throws on bad content. */
    public static Result load(Path file) {
        boolean created = false;
        String json;
        try {
            if (Files.notExists(file)) {
                if (file.getParent() != null) {
                    Files.createDirectories(file.getParent());
                }
                Files.writeString(file, DefaultConfig.json());
                created = true;
            }
            json = Files.readString(file);
        } catch (IOException e) {
            return new Result.Rejected(List.of("cannot read " + file + ": " + e.getMessage()));
        }
        return parse(json, created);
    }

    /** The settings of {@link DefaultConfig}. */
    public static ModSettings defaults() {
        return ((Result.Loaded) parse(DefaultConfig.json(), false)).settings();
    }

    private static Result parse(String json, boolean created) {
        var errors = new ArrayList<String>();
        ConfigResult curves = ConfigParser.parse(json);
        if (curves instanceof ConfigResult.Invalid invalid) {
            errors.addAll(invalid.errors());
        }
        JsonObject root = rootObject(json);
        var switches = new Switches(root, errors);
        boolean referenceDaycare = switches.read("referenceDaycare", false);
        boolean eggEventAdapter = switches.read("eggEventAdapter", false);
        boolean audit = switches.read("audit", true);
        if (!errors.isEmpty() || !(curves instanceof ConfigResult.Valid valid)) {
            return new Result.Rejected(errors);
        }
        return new Result.Loaded(new ModSettings(valid.config(), referenceDaycare, eggEventAdapter, audit), created);
    }

    private static JsonObject rootObject(String json) {
        try {
            JsonElement root = JsonParser.parseString(json);
            return root != null && root.isJsonObject() ? root.getAsJsonObject() : new JsonObject();
        } catch (JsonParseException e) {
            return new JsonObject();
        }
    }

    private record Switches(JsonObject root, List<String> errors) {
        boolean read(String block, boolean fallback) {
            JsonElement value = root.get(block);
            if (value == null || value.isJsonNull()) {
                return fallback;
            }
            if (!value.isJsonObject()) {
                errors.add(block + " = " + value + ": must be an object");
                return fallback;
            }
            JsonElement enabled = value.getAsJsonObject().get("enabled");
            if (enabled == null || enabled.isJsonNull()) {
                return fallback;
            }
            if (!enabled.isJsonPrimitive() || !enabled.getAsJsonPrimitive().isBoolean()) {
                errors.add(block + ".enabled = " + enabled + ": must be true or false");
                return fallback;
            }
            return enabled.getAsBoolean();
        }
    }
}
