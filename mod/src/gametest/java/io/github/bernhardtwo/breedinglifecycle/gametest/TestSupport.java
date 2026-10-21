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
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

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

    /** Runs {@code command} as {@code player} (or the server when null) and returns its output in {@code locale}. */
    static String run(GameTestHelper h, ServerPlayer player, int level, String command, String locale) {
        return run(h, player, level, command, locale, new ArrayList<>());
    }

    static String run(
            GameTestHelper h, ServerPlayer player, int level, String command, String locale, List<String> keys) {
        var messages = new ArrayList<Component>();
        h.getLevel().getServer().getCommands().performPrefixedCommand(stack(h, player, level, messages), command);
        LocalizationTarget target = () -> locale;
        var out = new StringBuilder();
        for (Component message : messages) {
            collectKeys(message, keys);
            out.append(Localization.text(message, target).getString()).append('\n');
        }
        return out.toString();
    }

    static void collectKeys(Component component, List<String> keys) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            keys.add(translatable.getKey());
        }
        component.getSiblings().forEach(sibling -> collectKeys(sibling, keys));
    }

    static CommandSourceStack stack(GameTestHelper h, ServerPlayer player, int level, List<Component> messages) {
        CommandSource capture = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) {
                messages.add(component);
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
        return new CommandSourceStack(
                capture,
                Vec3.ZERO,
                Vec2.ZERO,
                h.getLevel(),
                level,
                "test",
                Component.literal("test"),
                h.getLevel().getServer(),
                player);
    }
}
