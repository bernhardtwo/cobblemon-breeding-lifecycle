package io.github.bernhardtwo.breedinglifecycle.mod.daycare;

import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;

/** The offspring of a compatible pair: the root of the mother's line, or of the non-Ditto parent, at level 1. */
public final class Offspring {
    private Offspring() {}

    /** A new level 1 Pokémon of the root species of the line that gives the offspring; nothing is inherited. */
    public static Pokemon of(Pokemon a, Pokemon b) {
        FormData form = root(lineOf(a, b).getForm());
        return form.getSpecies().create(1);
    }

    private static Pokemon lineOf(Pokemon a, Pokemon b) {
        if (LifecycleService.isDitto(a)) {
            return b;
        }
        if (LifecycleService.isDitto(b)) {
            return a;
        }
        return b.getGender() == Gender.FEMALE ? b : a;
    }

    private static FormData root(FormData form) {
        FormData current = form;
        for (int depth = 0; depth < 16 && current.getPreEvolution() != null; depth++) {
            current = current.getPreEvolution().getForm();
        }
        return current;
    }
}
