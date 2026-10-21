package io.github.bernhardtwo.breedinglifecycle.gametest;

import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.species;

import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.DaycareData;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;

/** GameTests for the reference daycare's per-world saved data. */
public class DaycareDataGameTests implements FabricGameTest {
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000042");

    @GameTest(template = EMPTY_STRUCTURE)
    public void parentsAndOffspringSurviveSaveAndLoad(GameTestHelper h) {
        var d = new DaycareData();
        var p = species("charmander");
        BreedingCounter.increment(p);
        d.parents(PLAYER).add(p);
        d.offspring(PLAYER).add(species("charmander"));
        var copy = DaycareData.load(d.save(new CompoundTag(), reg(h)), reg(h));
        h.assertTrue(
                copy.parents(PLAYER).size() == 1
                        && BreedingCounter.get(copy.parents(PLAYER).get(0)) == 1
                        && copy.offspring(PLAYER).size() == 1,
                "roundtrip");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void loadsEntryWithoutOffspringList(GameTestHelper h) {
        var tag = new CompoundTag();
        var players = new CompoundTag();
        players.put(PLAYER.toString(), new CompoundTag());
        tag.put("players", players);
        var d = DaycareData.load(tag, reg(h));
        h.assertTrue(d.parents(PLAYER).isEmpty() && d.offspring(PLAYER).isEmpty(), "empty");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void unknownPlayerHasEmptyLists(GameTestHelper h) {
        h.assertTrue(new DaycareData().parents(UUID.randomUUID()).isEmpty(), "empty");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void serverInstanceIsShared(GameTestHelper h) {
        var s = h.getLevel().getServer();
        h.assertTrue(DaycareData.get(s) == DaycareData.get(s), "same");
        h.succeed();
    }

    private static HolderLookup.Provider reg(GameTestHelper h) {
        return h.getLevel().registryAccess();
    }
}
