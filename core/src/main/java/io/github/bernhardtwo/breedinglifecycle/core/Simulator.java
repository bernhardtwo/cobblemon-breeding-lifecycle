package io.github.bernhardtwo.breedinglifecycle.core;

import java.util.Arrays;
import java.util.TreeMap;

/** Monte Carlo simulation: each parent keeps breeding until its loss roll removes it. */
public final class Simulator {
    private Simulator() {}

    /** Simulates {@code parents} independent parents on {@code curve}. */
    public static SimulationResult run(Curve curve, int parents, RandomSource rng) {
        if (parents < 1) {
            throw new IllegalArgumentException("parents = " + parents + ": must be >= 1");
        }
        int[] offspring = new int[parents];
        var histogram = new TreeMap<Integer, Integer>();
        long total = 0;
        for (int i = 0; i < parents; i++) {
            int breedings = 0;
            boolean kept = true;
            while (kept) {
                kept = LossDecider.decide(curve, breedings, rng).survives();
                breedings++;
            }
            offspring[i] = breedings;
            total += breedings;
            histogram.merge(breedings, 1, Integer::sum);
        }
        Arrays.sort(offspring);
        return new SimulationResult(
                parents,
                (double) total / parents,
                percentile(offspring, 0.50),
                percentile(offspring, 0.90),
                percentile(offspring, 0.99),
                offspring[parents - 1],
                histogram);
    }

    private static int percentile(int[] sorted, double q) {
        int rank = (int) Math.ceil(q * sorted.length);
        return sorted[Math.max(rank, 1) - 1];
    }
}
