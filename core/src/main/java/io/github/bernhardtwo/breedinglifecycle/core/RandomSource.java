package io.github.bernhardtwo.breedinglifecycle.core;

/** Source of uniform random numbers, injected so results can be reproduced from a seed. */
@FunctionalInterface
public interface RandomSource {
    /** Returns a uniform value in {@code [0, 1)}. */
    double nextDouble();
}
