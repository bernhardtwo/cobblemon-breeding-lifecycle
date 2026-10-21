package io.github.bernhardtwo.breedinglifecycle.api;

import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;

/** Entry point for daycares: call {@link #evaluate} once per breeding, after generating the offspring. */
public final class BreedingLifecycle {
    private BreedingLifecycle() {}

    /**
     * Rolls each parent's loss, counts the breeding on both and audits it; the caller removes lost parents.
     *
     * @throws IllegalStateException off the server thread or before the server has started
     */
    public static BreedingResult evaluate(Pokemon parentA, Pokemon parentB) {
        return LifecycleService.get().evaluate(parentA, parentB, "api");
    }
}
