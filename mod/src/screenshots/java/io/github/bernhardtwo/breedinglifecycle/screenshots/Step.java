package io.github.bernhardtwo.breedinglifecycle.screenshots;

/** A chat command, without its slash, and the text its reply must contain. */
public record Step(String command, String expected) {}
