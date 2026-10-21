package io.github.bernhardtwo.breedinglifecycle.gametest;

import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.species;

import com.cobblemon.mod.common.Cobblemon;
import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

/** GameTests for {@code /breedinglifecycle}. */
public class CommandGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void curveListsExpectedOffspring(GameTestHelper h) {
        var out = run(h, null, 2, "breedinglifecycle curve", "en_us");
        h.assertTrue(out.contains("2.1981"), out);
        h.assertTrue(out.contains("0.4881"), out);
        h.assertTrue(out.contains("Ditto"), out);
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void curveIsTranslatedToSpanish(GameTestHelper h) {
        var out = run(h, null, 2, "breedinglifecycle curve", "es_mx");
        h.assertTrue(out.contains("Crías esperadas"), out);
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void infoShowsCounterOfPartySlot(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var pokemon = species("bulbasaur");
        BreedingCounter.increment(pokemon);
        BreedingCounter.increment(pokemon);
        Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemon);
        var out = run(h, player, 0, "breedinglifecycle info 1", "en_us");
        h.assertTrue(out.contains("breedings: 2"), out);
        h.assertTrue(out.contains("0.6089"), out);
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void infoOnEmptySlotExplains(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var keys = new ArrayList<String>();
        run(h, player, 0, "breedinglifecycle info 6", "en_us", keys);
        h.assertTrue(keys.contains("breedinglifecycle.info.empty_slot"), keys.toString());
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void infoNeedsAPlayer(GameTestHelper h) {
        var out = run(h, null, 4, "breedinglifecycle info 1", "en_us");
        h.assertTrue(out.contains("player"), out);
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void invalidReloadKeepsConfigAndReportsErrors(GameTestHelper h) {
        var file = TestSupport.configFile();
        try {
            String previous = Files.readString(file);
            try {
                Files.writeString(file, previous.replace("\"overrides\": {}", "\"overrides\": {\"r\": 0}"));
                var out = run(h, null, 2, "breedinglifecycle reload", "en_us");
                h.assertTrue(out.contains("overrides.r"), out);
                h.assertTrue(
                        LifecycleService.get()
                                .config()
                                .current()
                                .lifecycle()
                                .effective()
                                .equals(BreedingCurves.DEFAULT),
                        "kept default");
            } finally {
                Files.writeString(file, previous);
                LifecycleService.get().config().reload();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void validReloadReportsSuccess(GameTestHelper h) {
        var keys = new ArrayList<String>();
        run(h, null, 2, "breedinglifecycle reload", "en_us", keys);
        h.assertTrue(keys.contains("breedinglifecycle.reload.ok"), keys.toString());
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void reloadNeedsPermissionLevelTwo(GameTestHelper h) {
        var root =
                h.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("breedinglifecycle");
        h.assertTrue(root != null && root.canUse(stack(h, null, 0, new ArrayList<>())), "root usable at level 0");
        h.assertFalse(root.getChild("reload").canUse(stack(h, null, 0, new ArrayList<>())), "reload hidden at 0");
        h.assertTrue(root.getChild("reload").canUse(stack(h, null, 2, new ArrayList<>())), "reload at 2");
        h.succeed();
    }

    private static String run(GameTestHelper h, ServerPlayer player, int level, String command, String locale) {
        return run(h, player, level, command, locale, new ArrayList<>());
    }

    private static String run(
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

    private static void collectKeys(Component component, List<String> keys) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            keys.add(translatable.getKey());
        }
        component.getSiblings().forEach(sibling -> collectKeys(sibling, keys));
    }

    private static CommandSourceStack stack(
            GameTestHelper h, ServerPlayer player, int level, List<Component> messages) {
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
