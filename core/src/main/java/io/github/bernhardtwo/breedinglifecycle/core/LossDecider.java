package io.github.bernhardtwo.breedinglifecycle.core;

/** Rolls the loss of one parent after a breeding. */
public final class LossDecider {
    private LossDecider() {}

    /** Rolls breeding number {@code previousBreedings + 1}; the parent is lost when the roll is below {@code p}. */
    public static ParentOutcome decide(Curve curve, int previousBreedings, RandomSource rng) {
        if (previousBreedings < 0) {
            throw new IllegalArgumentException("previousBreedings = " + previousBreedings + ": must be >= 0");
        }
        int n = previousBreedings + 1;
        double p = curve.lossProbability(n);
        double roll = rng.nextDouble();
        return new ParentOutcome(n, p, roll, roll >= p);
    }
}
