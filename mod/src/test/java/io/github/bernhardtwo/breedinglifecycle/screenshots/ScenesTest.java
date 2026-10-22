package io.github.bernhardtwo.breedinglifecycle.screenshots;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScenesTest {
    @Test
    void statusScene() {
        assertThat(Scenes.status(1, 2))
                .containsExactly(
                        new Step("daycare deposit 1", "is now in the daycare."),
                        new Step("daycare deposit 2", "is now in the daycare."),
                        new Step("daycare breed", "was born and waits in the daycare."),
                        new Step("daycare status", "Waiting offspring (1): Charmander"));
    }

    @Test
    void infoScene() {
        assertThat(Scenes.info(3))
                .containsExactly(
                        new Step("daycare withdraw", "returned to your party or PC"),
                        new Step("breedinglifecycle info 3", "breedings: 1, loss chance on the next breeding: 0.4881"));
    }

    @Test
    void lossScene() {
        assertThat(Scenes.loss(2, 1))
                .containsExactly(
                        new Step("daycare deposit 2", "is now in the daycare."),
                        new Step("daycare deposit 1", "is now in the daycare."),
                        new Step("daycare breed", "Parent 2 is gone: breeding 1, loss chance 1.0000"));
    }

    @Test
    void refusedScene() {
        assertThat(Scenes.refused(1))
                .containsExactly(new Step(
                        "daycare deposit 1", "is out of its Poké Ball. Return it before leaving it at the daycare."));
    }

    @Test
    void freshPairClearsThePartyFirst() {
        assertThat(Scenes.fresh("Dev"))
                .containsExactly(
                        new Step("clearparty Dev", "'s party."),
                        new Step("pokegive charmander gender=male level=10", "Gave Charmander to"),
                        new Step("pokegive charmander gender=female level=10", "Gave Charmander to"));
    }

    @Test
    void emptyDaycareAcceptsBothReplies() {
        assertThat(Scenes.emptyDaycare())
                .isEqualTo(new Step("daycare withdraw", "returned to your party or PC", "Your daycare is empty."));
    }

    @Test
    void lossConfigForcesP1() {
        assertThat(Scenes.LOSS_CONFIG).contains("\"overrides\": { \"p1\": 1.0 }");
        assertThat(Scenes.DEFAULT_CONFIG)
                .contains("\"overrides\": {}")
                .contains("\"referenceDaycare\": { \"enabled\": true }");
        assertThat(Scenes.reload())
                .isEqualTo(new Step("breedinglifecycle reload", "Breeding Lifecycle config reloaded."));
    }
}
