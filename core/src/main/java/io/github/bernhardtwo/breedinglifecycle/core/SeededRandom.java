package io.github.bernhardtwo.breedinglifecycle.core;

import java.util.SplittableRandom;

/** {@link RandomSource} backed by {@link SplittableRandom}; the same seed gives the same sequence. */
public final class SeededRandom implements RandomSource {
    private final SplittableRandom random;

    public SeededRandom(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public SeededRandom() {
        this.random = new SplittableRandom();
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }
}
