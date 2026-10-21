package io.github.bernhardtwo.breedinglifecycle.gametest;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** Checks that the test server starts with Cobblemon and this mod. */
public class SmokeGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void cobblemonCreatesALevelOnePokemon(GameTestHelper helper) {
        var pokemon = PokemonSpecies.getByName("bulbasaur").create(1);
        helper.assertTrue(pokemon.getLevel() == 1, "level 1");
        helper.assertTrue(FabricLoader.getInstance().isModLoaded("breedinglifecycle"), "mod loaded");
        helper.succeed();
    }
}
