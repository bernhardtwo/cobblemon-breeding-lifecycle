package io.github.bernhardtwo.breedinglifecycle.core;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/** Offspring per parent over a Monte Carlo run; {@code histogram} maps offspring count to number of parents. */
public record SimulationResult(
        int parents, double mean, int p50, int p90, int p99, int max, SortedMap<Integer, Integer> histogram) {
    public SimulationResult {
        histogram = Collections.unmodifiableSortedMap(new TreeMap<>(histogram));
    }
}
