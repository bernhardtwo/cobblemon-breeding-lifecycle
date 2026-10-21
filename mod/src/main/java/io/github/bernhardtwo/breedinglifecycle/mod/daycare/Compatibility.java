package io.github.bernhardtwo.breedinglifecycle.mod.daycare;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import java.util.Collections;
import java.util.Optional;
import net.minecraft.nbt.Tag;

/** Cobbreeding's pairing rules, as used by the reference daycare. */
public final class Compatibility {
    /** Cobbreeding's root {@code persistentData} flag for a neutered Pokémon. */
    public static final String NEUTERED = "neutered";

    private static final String REJECT = "breedinglifecycle.daycare.reject.";

    private Compatibility() {}

    /** Empty when {@code a} and {@code b} can breed; otherwise the translation key of the reason. */
    public static Optional<String> rejection(Pokemon a, Pokemon b) {
        if (isNeutered(a) || isNeutered(b)) {
            return reject("neutered");
        }
        if (isUndiscovered(a) || isUndiscovered(b)) {
            return reject("undiscovered");
        }
        boolean dittoA = LifecycleService.isDitto(a);
        boolean dittoB = LifecycleService.isDitto(b);
        if (dittoA && dittoB) {
            return reject("two_dittos");
        }
        if (dittoA || dittoB) {
            return Optional.empty();
        }
        if (a.getGender() == Gender.GENDERLESS || b.getGender() == Gender.GENDERLESS) {
            return reject("genderless");
        }
        if (a.getGender() == b.getGender()) {
            return reject("same_gender");
        }
        if (Collections.disjoint(a.getForm().getEggGroups(), b.getForm().getEggGroups())) {
            return reject("no_egg_group");
        }
        return Optional.empty();
    }

    /** True when Cobbreeding's {@code neutered} flag is set to true. */
    public static boolean isNeutered(Pokemon pokemon) {
        var data = pokemon.getPersistentData();
        return data.contains(NEUTERED, Tag.TAG_BYTE) && data.getBoolean(NEUTERED);
    }

    private static boolean isUndiscovered(Pokemon pokemon) {
        return pokemon.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED);
    }

    private static Optional<String> reject(String reason) {
        return Optional.of(REJECT + reason);
    }
}
