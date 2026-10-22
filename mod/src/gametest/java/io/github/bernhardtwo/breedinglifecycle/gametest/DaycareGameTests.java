package io.github.bernhardtwo.breedinglifecycle.gametest;

import static com.cobblemon.mod.common.pokemon.Gender.FEMALE;
import static com.cobblemon.mod.common.pokemon.Gender.MALE;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.run;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.species;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.withConfig;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.withRandom;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.SuccessfulBattleStart;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.BreedOutcome;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.Daycare;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.DaycareData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** GameTests for the reference daycare service and {@code /daycare}. */
public class DaycareGameTests implements FabricGameTest {
    private static final String ENABLED = """
            {"presets": {"default": {"p1": 0.33, "r": 0.764}}, "referenceDaycare": {"enabled": true}}
            """;

    @GameTest(template = EMPTY_STRUCTURE)
    public void disabledDaycareChangesNothing(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var mon = mon("charmander", MALE);
        party(player).add(mon);
        var keys = new ArrayList<String>();
        run(h, player, 0, "daycare deposit 1", "en_us", keys);
        h.assertTrue(keys.contains("breedinglifecycle.daycare.disabled"), keys.toString());
        var breedKeys = new ArrayList<String>();
        run(h, player, 0, "daycare breed", "en_us", breedKeys);
        h.assertTrue(breedKeys.contains("breedinglifecycle.daycare.disabled"), breedKeys.toString());
        h.assertTrue(party(player).get(0) == mon, "still in the party");
        h.assertTrue(data(h).parents(player.getUUID()).isEmpty(), "daycare empty");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void depositMovesFromPartyToDaycare(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var mon = mon("charmander", MALE);
        party(player).add(mon);
        withConfig(ENABLED, () -> Daycare.deposit(player, 1));
        h.assertTrue(party(player).get(0) == null, "left the party");
        h.assertTrue(data(h).parents(player.getUUID()).equals(List.of(mon)), "in the daycare");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void depositRejectsThirdPokemon(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var third = mon("bulbasaur", MALE);
        party(player).add(mon("charmander", MALE));
        party(player).add(mon("charmander", FEMALE));
        party(player).add(third);
        var reply = new AtomicReference<Component>();
        withConfig(ENABLED, () -> {
            Daycare.deposit(player, 1);
            Daycare.deposit(player, 2);
            reply.set(Daycare.deposit(player, 3));
        });
        h.assertTrue(keys(reply.get()).contains("breedinglifecycle.daycare.full"), keys(reply.get()));
        h.assertTrue(party(player).get(2) == third, "third stays in the party");
        h.assertTrue(data(h).parents(player.getUUID()).size() == 2, "two parents");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void breedNeedsTwoParents(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var mon = mon("charmander", MALE);
        party(player).add(mon);
        var outcome = new AtomicReference<BreedOutcome>();
        withConfig(ENABLED, () -> {
            Daycare.deposit(player, 1);
            outcome.set(Daycare.breed(player));
        });
        h.assertTrue(
                outcome.get().equals(new BreedOutcome.Rejected("breedinglifecycle.daycare.need_two")),
                String.valueOf(outcome.get()));
        h.assertTrue(data(h).parents(player.getUUID()).size() == 1, "parent stays");
        h.assertTrue(data(h).offspring(player.getUUID()).isEmpty(), "no offspring");
        h.assertTrue(BreedingCounter.get(mon) == 0, "not counted");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void incompatiblePairIsRejectedWithReason(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var a = mon("charmander", MALE);
        var b = mon("charmander", MALE);
        var outcome = depositAndBreed(player, a, b, 0.0);
        h.assertTrue(
                outcome.equals(new BreedOutcome.Rejected("breedinglifecycle.daycare.reject.same_gender")),
                String.valueOf(outcome));
        h.assertTrue(BreedingCounter.get(a) == 0 && BreedingCounter.get(b) == 0, "counters 0");
        h.assertTrue(data(h).parents(player.getUUID()).size() == 2, "both stay");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void lowRollLosesBothAndKeepsOffspring(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var outcome = depositAndBreed(player, mon("charmander", MALE), mon("charmander", FEMALE), 0.0);
        h.assertTrue(outcome instanceof BreedOutcome.Bred, String.valueOf(outcome));
        h.assertTrue(data(h).parents(player.getUUID()).isEmpty(), "both lost");
        var offspring = data(h).offspring(player.getUUID());
        h.assertTrue(
                offspring.size() == 1
                        && offspring.get(0).getSpecies().getName().equalsIgnoreCase("charmander")
                        && offspring.get(0).getLevel() == 1,
                "one charmander at level 1");
        var lines = TestSupport.lastAuditLines(2);
        h.assertTrue(
                lines.stream()
                        .allMatch(line -> line.get("source").getAsString().equals("daycare")
                                && line.get("result").getAsString().equals("lost")
                                && line.get("playerUuid")
                                        .getAsString()
                                        .equals(player.getUUID().toString())),
                lines.toString());
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void highRollKeepsParentsAndCountsUp(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        depositAndBreed(player, mon("charmander", MALE), mon("charmander", FEMALE), 0.999);
        var parents = data(h).parents(player.getUUID());
        h.assertTrue(parents.size() == 2, "both kept");
        h.assertTrue(parents.stream().allMatch(p -> BreedingCounter.get(p) == 1), "counted once");
        h.assertTrue(data(h).offspring(player.getUUID()).size() == 1, "one offspring");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void breedingMarksDataDirtyOnce(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        party(player).add(mon("charmander", MALE));
        party(player).add(mon("charmander", FEMALE));
        var dirtyBefore = new AtomicReference<Boolean>();
        withConfig(
                ENABLED,
                () -> withRandom(() -> 0.0, () -> {
                    Daycare.deposit(player, 1);
                    Daycare.deposit(player, 2);
                    data(h).setDirty(false);
                    dirtyBefore.set(data(h).isDirty());
                    Daycare.breed(player);
                }));
        h.assertFalse(dirtyBefore.get(), "clean before");
        h.assertTrue(data(h).isDirty(), "dirty after");
        h.assertTrue(
                data(h).parents(player.getUUID()).isEmpty()
                        && data(h).offspring(player.getUUID()).size() == 1,
                "whole breeding visible on return");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void statusShowsCountersAndNextP(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var mon = mon("charmander", MALE);
        BreedingCounter.increment(mon);
        party(player).add(mon);
        var out = new AtomicReference<String>();
        withConfig(ENABLED, () -> {
            Daycare.deposit(player, 1);
            out.set(run(h, player, 0, "daycare status", "en_us"));
        });
        h.assertTrue(out.get().contains("0.4881"), out.get());
        h.assertTrue(out.get().contains("breedings: 1"), out.get());
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void withdrawReturnsParentsAndOffspring(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        party(player).add(mon("charmander", MALE));
        party(player).add(mon("charmander", FEMALE));
        withConfig(
                ENABLED,
                () -> withRandom(() -> 0.999, () -> {
                    Daycare.deposit(player, 1);
                    Daycare.deposit(player, 2);
                    Daycare.breed(player);
                    Daycare.withdraw(player);
                }));
        h.assertTrue(
                party(player).occupied() == 3,
                "three in the party, got " + party(player).occupied());
        h.assertTrue(data(h).parents(player.getUUID()).isEmpty(), "no parents left");
        h.assertTrue(data(h).offspring(player.getUUID()).isEmpty(), "no offspring left");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void withdrawWithFullPartyGoesToPc(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var deposited = mon("charmander", MALE);
        party(player).add(deposited);
        withConfig(ENABLED, () -> {
            Daycare.deposit(player, 1);
            for (int i = 0; i < 6; i++) {
                party(player).add(species("bulbasaur"));
            }
            Daycare.withdraw(player);
        });
        var pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        h.assertTrue(pc.get(deposited.getUuid()) == deposited, "in the PC");
        h.assertTrue(data(h).parents(player.getUUID()).isEmpty(), "daycare empty");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void withdrawKeepsWhatFitsNowhere(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var deposited = mon("charmander", MALE);
        party(player).add(deposited);
        var pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        var reply = new AtomicReference<Component>();
        withConfig(ENABLED, () -> {
            Daycare.deposit(player, 1);
            for (int i = 0; i < 6; i++) {
                party(player).add(species("bulbasaur"));
            }
            pc.resize(1, false, pokemon -> kotlin.Unit.INSTANCE);
            while (pc.getFirstAvailablePosition() != null) {
                pc.add(species("bulbasaur"));
            }
            reply.set(Daycare.withdraw(player));
        });
        h.assertTrue(keys(reply.get()).contains("breedinglifecycle.daycare.withdrawn_partly"), keys(reply.get()));
        h.assertTrue(data(h).parents(player.getUUID()).equals(List.of(deposited)), "stays in the daycare");
        h.assertTrue(pc.get(deposited.getUuid()) == null, "not in the PC");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void depositIsRefusedDuringABattle(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var mon = mon("charmander", MALE);
        party(player).add(mon);
        var wild = new PokemonEntity(h.getLevel(), species("bulbasaur"), CobblemonEntities.POKEMON);
        wild.setPos(player.position());
        h.getLevel().addFreshEntity(wild);
        var start = BattleBuilder.INSTANCE.pve(player, wild);
        h.assertTrue(start instanceof SuccessfulBattleStart, "battle started: " + start);
        var battle = ((SuccessfulBattleStart) start).getBattle();
        var reply = new AtomicReference<Component>();
        try {
            withConfig(ENABLED, () -> reply.set(Daycare.deposit(player, 1)));
        } finally {
            BattleRegistry.closeBattle(battle);
            wild.discard();
        }
        h.assertTrue(keys(reply.get()).contains("breedinglifecycle.daycare.in_battle"), keys(reply.get()));
        h.assertTrue(party(player).get(0) == mon, "still in the party");
        h.assertTrue(data(h).parents(player.getUUID()).isEmpty(), "daycare empty");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void statusWorksWhileDisabled(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        party(player).add(mon("charmander", MALE));
        withConfig(ENABLED, () -> Daycare.deposit(player, 1));
        var keys = new ArrayList<String>();
        run(h, player, 0, "daycare status", "en_us", keys);
        h.assertTrue(keys.contains("breedinglifecycle.daycare.status.parent"), keys.toString());
        h.assertFalse(keys.contains("breedinglifecycle.daycare.disabled"), keys.toString());
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void withdrawWorksWhileDisabled(GameTestHelper h) {
        ServerPlayer player = h.makeMockServerPlayerInLevel();
        var mon = mon("charmander", MALE);
        party(player).add(mon);
        withConfig(ENABLED, () -> Daycare.deposit(player, 1));
        var reply = Daycare.withdraw(player);
        h.assertTrue(keys(reply).contains("breedinglifecycle.daycare.withdrawn"), keys(reply));
        h.assertTrue(party(player).get(0) == mon, "back in the party");
        h.assertTrue(data(h).parents(player.getUUID()).isEmpty(), "daycare empty");
        h.succeed();
    }

    private static BreedOutcome depositAndBreed(ServerPlayer player, Pokemon a, Pokemon b, double roll) {
        party(player).add(a);
        party(player).add(b);
        var outcome = new AtomicReference<BreedOutcome>();
        withConfig(
                ENABLED,
                () -> withRandom(() -> roll, () -> {
                    Daycare.deposit(player, 1);
                    Daycare.deposit(player, 2);
                    outcome.set(Daycare.breed(player));
                }));
        return outcome.get();
    }

    private static Pokemon mon(String name, Gender gender) {
        var pokemon = species(name);
        pokemon.setGender(gender);
        return pokemon;
    }

    private static PlayerPartyStore party(ServerPlayer player) {
        return Cobblemon.INSTANCE.getStorage().getParty(player);
    }

    private static DaycareData data(GameTestHelper h) {
        return DaycareData.get(h.getLevel().getServer());
    }

    private static String keys(Component component) {
        var keys = new ArrayList<String>();
        TestSupport.collectKeys(component, keys);
        return keys.toString();
    }
}
