package io.github.bernhardtwo.breedinglifecycle.mod.daycare;

import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingResult;

/** What {@link Daycare#breed} did: an offspring with the parents' rolls, or the reason nothing happened. */
public sealed interface BreedOutcome permits BreedOutcome.Bred, BreedOutcome.Rejected {
    /** The offspring now waiting in the daycare and the roll of each parent, in deposit order. */
    record Bred(Pokemon offspring, BreedingResult result) implements BreedOutcome {}

    /** Nothing changed; {@code reasonKey} is the translation key of the reason. */
    record Rejected(String reasonKey) implements BreedOutcome {}
}
