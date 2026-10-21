package io.github.bernhardtwo.breedinglifecycle.gametest;

import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.species;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.withConfig;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.withRandom;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.CollectEggEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/** GameTests for the optional {@code COLLECT_EGG} adapter. */
public class EggEventAdapterGameTests implements FabricGameTest {
    private static final String ENABLED = """
            {"presets": {"default": {"p1": 0.33, "r": 0.764}}, "eggEventAdapter": {"enabled": true}}
            """;

    @GameTest(template = EMPTY_STRUCTURE)
    public void collectEggWithPartyParentsRemovesLostOnes(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        var male = species("bulbasaur");
        var female = species("bulbasaur");
        party.add(male);
        party.add(female);
        withConfig(ENABLED, () -> withRandom(() -> 0.0, () -> collect(male, female, player)));
        h.assertTrue(party.get(0) == null && party.get(1) == null, "both lost parents left the party");
        h.assertTrue(BreedingCounter.get(male) == 1 && BreedingCounter.get(female) == 1, "counters 1");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void keptParentsStayAndCountUp(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        var male = species("bulbasaur");
        var female = species("bulbasaur");
        party.add(male);
        party.add(female);
        withConfig(ENABLED, () -> withRandom(() -> 0.999, () -> collect(male, female, player)));
        h.assertTrue(party.get(0) == male && party.get(1) == female, "kept parents stay");
        h.assertTrue(BreedingCounter.get(male) == 1, "counted");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void pasturedParentIsUntetheredAndLeavesItsPc(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        var male = species("bulbasaur");
        var female = species("bulbasaur");
        pc.add(male);
        pc.add(female);
        male.setTetheringId(UUID.randomUUID());
        withConfig(ENABLED, () -> withRandom(() -> 0.0, () -> collect(male, female, player)));
        h.assertTrue(male.getTetheringId() == null, "untethered");
        h.assertTrue(pc.get(male.getUuid()) == null && pc.get(female.getUuid()) == null, "left the PC");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeholderParentsAreIgnored(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        var own = species("bulbasaur");
        party.add(own);
        var male = new Pokemon();
        var female = new Pokemon();
        long before = auditLines();
        withConfig(ENABLED, () -> withRandom(() -> 0.0, () -> collect(male, female, player)));
        h.assertTrue(party.get(0) == own, "party intact");
        h.assertTrue(BreedingCounter.get(male) == 0 && BreedingCounter.get(female) == 0, "counters 0");
        h.assertTrue(auditLines() == before, "nothing audited");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void disabledAdapterDoesNothing(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        var male = species("bulbasaur");
        var female = species("bulbasaur");
        party.add(male);
        party.add(female);
        h.assertFalse(LifecycleService.get().config().current().eggEventAdapter(), "disabled by default");
        withRandom(() -> 0.0, () -> collect(male, female, player));
        h.assertTrue(party.get(0) == male && party.get(1) == female, "parents intact");
        h.assertTrue(BreedingCounter.get(male) == 0, "not counted");
        h.succeed();
    }

    private static void collect(Pokemon male, Pokemon female, ServerPlayer player) {
        var egg = PokemonProperties.Companion.parse("species=bulbasaur level=1");
        CobblemonEvents.COLLECT_EGG.emit(new CollectEggEvent(egg, male, female, player));
    }

    private static long auditLines() {
        LifecycleService.get().audit().flush();
        Path file = FabricLoader.getInstance().getGameDir().resolve("logs/breeding-lifecycle/audit.jsonl");
        try {
            return Files.exists(file) ? Files.readAllLines(file).size() : 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
