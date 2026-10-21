package io.github.bernhardtwo.breedinglifecycle.core;

import java.util.Objects;

/** The curve for regular parents and the separate curve used when the parent is a Ditto. */
public record BreedingCurves(Curve main, Curve ditto) {
    /** The {@code default} preset: both curves are {@link Curve#DEFAULT}. */
    public static final BreedingCurves DEFAULT = new BreedingCurves(Curve.DEFAULT, Curve.DEFAULT);

    public BreedingCurves {
        Objects.requireNonNull(main, "main");
        Objects.requireNonNull(ditto, "ditto");
    }

    /** Returns the curve that applies to a parent. */
    public Curve forParent(boolean isDitto) {
        return isDitto ? ditto : main;
    }
}
