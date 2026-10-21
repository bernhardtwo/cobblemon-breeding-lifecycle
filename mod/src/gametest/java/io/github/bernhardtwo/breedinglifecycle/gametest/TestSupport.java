package io.github.bernhardtwo.breedinglifecycle.gametest;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bernhardtwo.breedinglifecycle.core.RandomSource;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

final class TestSupport {
    private TestSupport() {}

    static Pokemon species(String name) {
        return PokemonSpecies.getByName(name).create(1);
    }

    static void withRandom(RandomSource rng, Runnable body) {
        LifecycleService.get().setRandomForTesting(rng);
        try {
            body.run();
        } finally {
            LifecycleService.get().setRandomForTesting(null);
        }
    }

    /** The last {@code count} audit lines, after every queued entry is written. */
    static List<JsonObject> lastAuditLines(int count) {
        LifecycleService.get().audit().flush();
        Path file = FabricLoader.getInstance().getGameDir().resolve("logs/breeding-lifecycle/audit.jsonl");
        try {
            List<String> lines = Files.readAllLines(file);
            return lines.subList(Math.max(0, lines.size() - count), lines.size()).stream()
                    .map(line -> JsonParser.parseString(line).getAsJsonObject())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("breeding-lifecycle.json");
    }

    /** Runs {@code body} with {@code json} as the active config, then restores the previous file and settings. */
    static void withConfig(String json, Runnable body) {
        Path file = configFile();
        try {
            String previous = Files.readString(file);
            Files.writeString(file, json);
            try {
                var errors = LifecycleService.get().config().reload();
                if (!errors.isEmpty()) {
                    throw new IllegalStateException("test config rejected: " + errors);
                }
                body.run();
            } finally {
                Files.writeString(file, previous);
                LifecycleService.get().config().reload();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
