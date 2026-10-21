package io.github.bernhardtwo.breedinglifecycle.gametest;

import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.species;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.withConfig;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.withRandom;

import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingLifecycle;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingResult;
import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;

/** GameTests for {@code BreedingLifecycle.evaluate} and the breeding counter. */
public class EvaluateGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void missingCounterReadsAsZero(GameTestHelper h) {
        var p = species("bulbasaur");
        h.assertTrue(BreedingCounter.get(p) == 0, "0");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void corruptCounterReadsAsZero(GameTestHelper h) {
        var p = species("bulbasaur");
        var tag = new CompoundTag();
        tag.putString("breedings", "x");
        p.getPersistentData().put("breedinglifecycle", tag);
        h.assertTrue(BreedingCounter.get(p) == 0, "string");
        tag.putInt("breedings", -4);
        h.assertTrue(BreedingCounter.get(p) == 0, "negative");
        p.getPersistentData().putString("breedinglifecycle", "not a compound");
        h.assertTrue(BreedingCounter.get(p) == 0, "not a compound");
        BreedingCounter.increment(p);
        h.assertTrue(BreedingCounter.get(p) == 1, "increment replaces a corrupt value");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void lowRollLosesBothParents(GameTestHelper h) {
        withRandom(() -> 0.0, () -> {
            var r = BreedingLifecycle.evaluate(species("bulbasaur"), species("bulbasaur"));
            h.assertFalse(r.parentA().survives() || r.parentB().survives(), "both lost");
            h.assertTrue(r.parentA().n() == 1 && Math.abs(r.parentA().p() - 0.33) < 1e-9, "n=1 p=0.33");
        });
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void highRollKeepsAndCountsUp(GameTestHelper h) {
        withRandom(() -> 0.999, () -> {
            var a = species("bulbasaur");
            var b = species("bulbasaur");
            BreedingLifecycle.evaluate(a, b);
            var r = BreedingLifecycle.evaluate(a, species("bulbasaur"));
            h.assertTrue(BreedingCounter.get(a) == 2 && r.parentA().n() == 2, "n=2");
            h.assertTrue(BreedingCounter.get(b) == 1, "other parent counted once");
            h.assertTrue(r.parentA().survives() && r.parentA().pokemon().equals(a.getUuid()), "kept, by uuid");
            h.assertTrue(Math.abs(r.parentA().p() - 0.4881) < 1e-4, "p(2)");
        });
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void counterSurvivesSaveAndLoad(GameTestHelper h) {
        var a = species("bulbasaur");
        BreedingCounter.increment(a);
        var registries = h.getLevel().registryAccess();
        var copy = Pokemon.Companion.loadFromNBT(registries, a.saveToNBT(registries, new CompoundTag()));
        h.assertTrue(BreedingCounter.get(copy) == 1, "persisted");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void dittoUsesDittoCurve(GameTestHelper h) {
        String json = """
                {"presets": {"default": {"p1": 0, "r": 0.5, "ditto": {"p1": 1.0, "r": 0.5}}}}
                """;
        withConfig(
                json,
                () -> withRandom(() -> 0.5, () -> {
                    BreedingResult r = BreedingLifecycle.evaluate(species("ditto"), species("bulbasaur"));
                    h.assertFalse(r.parentA().survives(), "ditto lost with p=1");
                    h.assertTrue(r.parentA().p() == 1.0, "ditto p");
                    h.assertTrue(r.parentB().survives() && r.parentB().p() == 0.0, "partner kept with p=0");
                }));
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void offThreadCallIsRejected(GameTestHelper h) {
        var a = species("bulbasaur");
        var b = species("bulbasaur");
        var thrown = new AtomicReference<Throwable>();
        var thread = new Thread(() -> {
            try {
                BreedingLifecycle.evaluate(a, b);
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        h.assertTrue(thrown.get() instanceof IllegalStateException, "IllegalStateException, got " + thrown.get());
        h.assertTrue(BreedingCounter.get(a) == 0, "counter untouched");
        h.succeed();
    }
}
