package io.github.bernhardtwo.breedinglifecycle.screenshots;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReplyWaitTest {
    @Test
    void ignoresMessagesBeforeTheCommand() {
        ReplyWait wait = new ReplyWait(new Step("daycare breed", "was born"), 1_000, 30_000);
        assertThat(wait.accept("A Charmander was born and waits in the daycare.", 999))
                .isFalse();
        assertThat(wait.accept("A Charmander was born and waits in the daycare.", 1_001))
                .isTrue();
    }

    @Test
    void ignoresOtherMessages() {
        ReplyWait wait = new ReplyWait(new Step("daycare breed", "was born"), 0, 30_000);
        assertThat(wait.accept("The daycare needs two Pokémon to breed.", 5)).isFalse();
    }

    @Test
    void timesOutWithTheCommandAndText() {
        ReplyWait wait = new ReplyWait(new Step("daycare breed", "was born"), 0, 30_000);
        assertThat(wait.failure(29_999)).isEmpty();
        assertThat(wait.failure(30_000)).contains("timed out waiting for 'was born' after /daycare breed");
    }
}
