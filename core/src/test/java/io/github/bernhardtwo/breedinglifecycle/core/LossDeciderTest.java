package io.github.bernhardtwo.breedinglifecycle.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LossDeciderTest {
    @Test
    void rollBelowPLoses() {
        assertThat(LossDecider.decide(Curve.DEFAULT, 0, () -> 0.329).survives()).isFalse();
    }

    @Test
    void rollAtPSurvives() {
        double p = Curve.DEFAULT.lossProbability(1);
        var outcome = LossDecider.decide(Curve.DEFAULT, 0, () -> p);
        assertThat(outcome.survives()).isTrue();
        assertThat(outcome.n()).isEqualTo(1);
    }

    @Test
    void previousBreedingsSetTheBreedingNumber() {
        var outcome = LossDecider.decide(Curve.DEFAULT, 1, () -> 0.5);
        assertThat(outcome.n()).isEqualTo(2);
        assertThat(outcome.p()).isEqualTo(Curve.DEFAULT.lossProbability(2));
        assertThat(outcome.roll()).isEqualTo(0.5);
        assertThat(outcome.survives()).isTrue();
    }
}
