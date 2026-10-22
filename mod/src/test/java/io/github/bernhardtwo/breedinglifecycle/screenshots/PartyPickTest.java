package io.github.bernhardtwo.breedinglifecycle.screenshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyPickTest {
    @Test
    void slotIsOneBased() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        assertThat(PartyPick.slotOf(Arrays.asList(null, a, b), b)).isEqualTo(3);
    }

    @Test
    void missingPokemonFails() {
        UUID gone = UUID.randomUUID();
        assertThatThrownBy(() -> PartyPick.slotOf(Arrays.asList(UUID.randomUUID(), null), gone))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(gone.toString());
    }
}
