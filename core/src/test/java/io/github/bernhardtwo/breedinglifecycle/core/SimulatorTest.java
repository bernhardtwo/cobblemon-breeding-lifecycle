package io.github.bernhardtwo.breedinglifecycle.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class SimulatorTest {
    @Test
    void sameSeedSameResult() {
        assertThat(Simulator.run(Curve.DEFAULT, 10_000, new SeededRandom(42)))
                .isEqualTo(Simulator.run(Curve.DEFAULT, 10_000, new SeededRandom(42)));
    }

    @Test
    void meanConvergesToE() {
        assertThat(Simulator.run(Curve.DEFAULT, 200_000, new SeededRandom(7)).mean())
                .isCloseTo(2.1981, within(0.02));
    }

    @Test
    void certainLossGivesExactlyOne() {
        var result = Simulator.run(new Curve(1.0, 0.5, null), 1000, new SeededRandom(1));
        assertThat(result.max()).isEqualTo(1);
        assertThat(result.p50()).isEqualTo(1);
        assertThat(result.histogram()).containsExactly(org.assertj.core.api.Assertions.entry(1, 1000));
    }

    @Test
    void maxBreedingsCapsOffspring() {
        assertThat(Simulator.run(new Curve(0.0, 0.99, 3), 5000, new SeededRandom(5))
                        .max())
                .isLessThanOrEqualTo(3);
    }

    @Test
    void nonPositiveParentsRejected() {
        assertThatThrownBy(() -> Simulator.run(Curve.DEFAULT, 0, new SeededRandom(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
