package io.github.bernhardtwo.breedinglifecycle.api;

import java.util.Objects;

/** The outcome of one breeding, one {@link ParentResult} per parent, in the order they were passed. */
public record BreedingResult(ParentResult parentA, ParentResult parentB) {
    public BreedingResult {
        Objects.requireNonNull(parentA, "parentA");
        Objects.requireNonNull(parentB, "parentB");
    }
}
