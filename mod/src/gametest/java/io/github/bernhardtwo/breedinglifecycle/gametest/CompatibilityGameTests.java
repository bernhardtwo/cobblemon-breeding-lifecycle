package io.github.bernhardtwo.breedinglifecycle.gametest;

import static com.cobblemon.mod.common.pokemon.Gender.FEMALE;
import static com.cobblemon.mod.common.pokemon.Gender.MALE;
import static io.github.bernhardtwo.breedinglifecycle.gametest.TestSupport.species;

import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.Compatibility;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.Offspring;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** GameTests for the reference daycare's compatibility rules and offspring species. */
public class CompatibilityGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void maleAndFemaleWithSharedGroupBreed(GameTestHelper h) {
        h.assertTrue(
                Compatibility.rejection(mon("charmander", MALE), mon("charmander", FEMALE))
                        .isEmpty(),
                "ok");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void undiscoveredIsRejected(GameTestHelper h) {
        assertReject(h, mon("pichu", MALE), mon("pikachu", FEMALE), "undiscovered");
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void neuteredParentIsRejected(GameTestHelper h) {
        var f = mon("charmander", FEMALE);
        f.getPersistentData().putBoolean("neutered", true);
        assertReject(h, mon("charmander", MALE), f, "neutered");
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void explicitFalseNeuteredBreeds(GameTestHelper h) {
        var f = mon("charmander", FEMALE);
        f.getPersistentData().putBoolean("neutered", false);
        h.assertTrue(Compatibility.rejection(mon("charmander", MALE), f).isEmpty(), "ok");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void twoDittosAreRejected(GameTestHelper h) {
        assertReject(h, species("ditto"), species("ditto"), "two_dittos");
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void genderlessOnlyWithDitto(GameTestHelper h) {
        h.assertTrue(
                Compatibility.rejection(species("magnemite"), mon("charmander", FEMALE))
                        .equals(Optional.of("breedinglifecycle.daycare.reject.genderless")),
                "genderless rejected");
        h.assertTrue(
                Compatibility.rejection(species("magnemite"), species("ditto")).isEmpty(), "ditto ok");
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void sameGenderIsRejected(GameTestHelper h) {
        assertReject(h, mon("charmander", MALE), mon("charmander", MALE), "same_gender");
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void noSharedEggGroupIsRejected(GameTestHelper h) {
        assertReject(h, mon("bulbasaur", MALE), mon("magikarp", FEMALE), "no_egg_group");
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void offspringIsRootOfFemaleLine(GameTestHelper h) {
        var o = Offspring.of(mon("charmander", MALE), mon("charizard", FEMALE));
        h.assertTrue(
                o.getSpecies().getName().equalsIgnoreCase("charmander") && o.getLevel() == 1,
                o.getSpecies().getName());
        h.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void offspringWithDittoIsTheOtherParent(GameTestHelper h) {
        var o = Offspring.of(species("ditto"), mon("ivysaur", MALE));
        h.assertTrue(
                o.getSpecies().getName().equalsIgnoreCase("bulbasaur"),
                o.getSpecies().getName());
        h.succeed();
    }

    private static Pokemon mon(String name, Gender gender) {
        var pokemon = species(name);
        pokemon.setGender(gender);
        return pokemon;
    }

    private static void assertReject(GameTestHelper h, Pokemon a, Pokemon b, String reason) {
        var got = Compatibility.rejection(a, b);
        h.assertTrue(got.equals(Optional.of("breedinglifecycle.daycare.reject." + reason)), String.valueOf(got));
        h.succeed();
    }
}
