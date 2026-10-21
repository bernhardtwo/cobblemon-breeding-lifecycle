package io.github.bernhardtwo.breedinglifecycle.api;

import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;

/** Entry point for daycares: call {@link #evaluate} once per breeding, after generating the offspring. */
public final class BreedingLifecycle {
    private BreedingLifecycle() {}

    /**
     * Rolls the loss of each parent, adds one breeding to both counters and audits it. The caller removes lost parents.
     *
     * @throws IllegalStateException when called off the server thread or before the server has started
     */
    public static BreedingResult evaluate(Pokemon parentA, Pokemon parentB) {
        return LifecycleService.get().evaluate(parentA, parentB, "api");
    }
}
