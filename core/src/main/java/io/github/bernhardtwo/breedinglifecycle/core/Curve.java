package io.github.bernhardtwo.breedinglifecycle.core;

import java.util.ArrayList;
import java.util.List;

/** Loss curve of one parent; {@code maxBreedings} is the breeding at which it is always lost, or null. */
public record Curve(double p1, double r, Integer maxBreedings) {
    private static final double SURVIVAL_EPSILON = 1e-12;
    private static final int MAX_UNLIMITED_BREEDINGS = 1_000_000;

    /** The {@code default} preset curve: {@code p1 = 0.33}, {@code r = 0.764}, no limit. */
    public static final Curve DEFAULT = new Curve(0.33, 0.764, null);

    public Curve {
        var errors = validate(p1, r, maxBreedings);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }

    /** Returns one message per invalid field, each starting with the field name; empty when the curve is valid. */
    public static List<String> validate(double p1, double r, Integer maxBreedings) {
        var errors = new ArrayList<String>();
        if (!(p1 >= 0 && p1 <= 1)) {
            errors.add("p1 = " + p1 + ": must be in [0, 1]");
        }
        if (!(r > 0 && r <= 1)) {
            errors.add("r = " + r + ": must be in (0, 1]");
        }
        if (maxBreedings != null && maxBreedings < 1) {
            errors.add("maxBreedings = " + maxBreedings + ": must be null or an integer >= 1");
        }
        if (errors.isEmpty() && maxBreedings == null) {
            if (p1 == 0 && r == 1) {
                errors.add("p1 = 0 and r = 1: a parent would never be lost; set maxBreedings");
            } else if (!survivalVanishes(p1, r)) {
                errors.add("p1 = " + p1 + " and r = " + r
                        + ": a parent would practically never be lost (still there after " + MAX_UNLIMITED_BREEDINGS
                        + " breedings); set maxBreedings");
            }
        }
        return errors;
    }

    /** Probability of losing the parent in its {@code n}-th breeding, {@code n >= 1}. */
    public double lossProbability(int n) {
        requirePositive(n);
        if (maxBreedings != null && n >= maxBreedings) {
            return 1.0;
        }
        return 1.0 - (1.0 - p1) * Math.pow(r, n - 1);
    }

    /** Probability that the parent is still there for its {@code n}-th breeding. */
    public double survival(int n) {
        requirePositive(n);
        double survival = 1.0;
        for (int k = 1; k < n; k++) {
            survival *= 1.0 - lossProbability(k);
        }
        return survival;
    }

    /** Expected number of offspring per parent, the sum of {@code survival(n)}. */
    public double expectedOffspring() {
        double expected = 0.0;
        double survival = 1.0;
        for (int n = 1; survival >= SURVIVAL_EPSILON && (maxBreedings == null || n <= maxBreedings); n++) {
            expected += survival;
            survival *= 1.0 - lossProbability(n);
        }
        return expected;
    }

    private static boolean survivalVanishes(double p1, double r) {
        double survival = 1.0;
        for (int n = 1; n <= MAX_UNLIMITED_BREEDINGS; n++) {
            survival *= (1.0 - p1) * Math.pow(r, n - 1);
            if (survival < SURVIVAL_EPSILON) {
                return true;
            }
        }
        return false;
    }

    private static void requirePositive(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n = " + n + ": must be >= 1");
        }
    }
}
