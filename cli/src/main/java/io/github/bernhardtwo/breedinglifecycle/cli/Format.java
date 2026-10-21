package io.github.bernhardtwo.breedinglifecycle.cli;

import io.github.bernhardtwo.breedinglifecycle.core.Curve;

final class Format {
    private Format() {}

    static String curveHeader(Messages messages, String label, Curve curve) {
        String max = curve.maxBreedings() == null
                ? messages.get("curve.noLimit")
                : curve.maxBreedings().toString();
        return messages.get("curve.header", label, messages.decimal(curve.p1()), messages.decimal(curve.r()), max);
    }
}
