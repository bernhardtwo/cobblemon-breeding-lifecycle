package io.github.bernhardtwo.breedinglifecycle.api;

import java.util.UUID;

/**
 * The roll for one parent: its breeding number {@code n}, the loss chance {@code p}, the roll and whether it stays.
 */
public record ParentResult(UUID pokemon, int n, double p, double roll, boolean survives) {}
