package io.github.bernhardtwo.breedinglifecycle.core;

/** Result of one parent's loss roll: its breeding number, the loss chance, the roll and whether it stays. */
public record ParentOutcome(int n, double p, double roll, boolean survives) {}
