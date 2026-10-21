package io.github.bernhardtwo.breedinglifecycle.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class CurveTest {
    @Test
    void defaultCurveMatchesBaseline() {
        assertThat(Curve.DEFAULT.lossProbability(2)).isCloseTo(0.4881, within(1e-4));
        assertThat(Curve.DEFAULT.survival(3)).isCloseTo(0.343, within(1e-4));
        assertThat(Curve.DEFAULT.expectedOffspring()).isCloseTo(2.1981, within(1e-4));
    }

    @Test
    void maxBreedingsForcesCertainLoss() {
        assertThat(new Curve(0.1, 1.0, 3).lossProbability(3)).isEqualTo(1.0);
    }

    @Test
    void infiniteCurveIsRejected() {
        assertThatThrownBy(() -> new Curve(0.0, 1.0, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void outOfRangeIsRejected() {
        assertThatThrownBy(() -> new Curve(0.33, 0.0, null)).hasMessageContaining("r");
    }

    @Test
    void nBelowOneIsRejected() {
        assertThatThrownBy(() -> Curve.DEFAULT.lossProbability(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dittoParentUsesDittoCurve() {
        var ditto = new Curve(0.5, 0.9, null);
        var curves = new BreedingCurves(Curve.DEFAULT, ditto);
        assertThat(curves.forParent(true)).isEqualTo(ditto);
        assertThat(curves.forParent(false)).isEqualTo(Curve.DEFAULT);
        assertThat(BreedingCurves.DEFAULT.ditto()).isEqualTo(Curve.DEFAULT);
    }
}
