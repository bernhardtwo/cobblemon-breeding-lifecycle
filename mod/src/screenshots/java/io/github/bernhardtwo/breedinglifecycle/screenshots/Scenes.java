package io.github.bernhardtwo.breedinglifecycle.screenshots;

import java.util.List;

/** The commands of each in-game screenshot and the replies they wait for, as in the server's en_us texts. */
public final class Scenes {
    private static final String DEPOSITED = "is now in the daycare.";

    /** The server config of {@code scripts/ingame-screenshots.sh} with the default curve. */
    public static final String DEFAULT_CONFIG = config("{}");

    /** The same config with {@code p1 = 1.0}, so every parent is lost on its first breeding. */
    public static final String LOSS_CONFIG = config("{ \"p1\": 1.0 }");

    private Scenes() {}

    public static List<Step> fresh(String player) {
        return List.of(
                new Step("clearparty " + player, "'s party."),
                new Step("pokegive charmander gender=male level=10", "Gave Charmander to"),
                new Step("pokegive charmander gender=female level=10", "Gave Charmander to"));
    }

    /** Empties this player's daycare, whatever it holds from an earlier run. */
    public static Step emptyDaycare() {
        return new Step("daycare withdraw", "returned to your party or PC", "Your daycare is empty.");
    }

    public static List<Step> status(int male, int female) {
        return List.of(
                new Step("daycare deposit " + male, DEPOSITED),
                new Step("daycare deposit " + female, DEPOSITED),
                new Step("daycare breed", "was born and waits in the daycare."),
                new Step("daycare status", "Waiting offspring (1): Charmander"));
    }

    public static List<Step> info(int slot) {
        return List.of(
                new Step("daycare withdraw", "returned to your party or PC"),
                new Step("breedinglifecycle info " + slot, "breedings: 1, loss chance on the next breeding: 0.4881"));
    }

    public static List<Step> loss(int male, int female) {
        return List.of(
                new Step("daycare deposit " + male, DEPOSITED),
                new Step("daycare deposit " + female, DEPOSITED),
                new Step("daycare breed", "Parent 2 is gone: breeding 1, loss chance 1.0000"));
    }

    public static List<Step> refused(int slot) {
        return List.of(new Step(
                "daycare deposit " + slot, "is out of its Poké Ball. Return it before leaving it at the daycare."));
    }

    public static Step reload() {
        return new Step("breedinglifecycle reload", "Breeding Lifecycle config reloaded.");
    }

    private static String config(String overrides) {
        return """
                {
                  "preset": "default",
                  "presets": { "default": { "p1": 0.33, "r": 0.764, "maxBreedings": null } },
                  "overrides": %s,
                  "referenceDaycare": { "enabled": true },
                  "eggEventAdapter": { "enabled": false },
                  "audit": { "enabled": true }
                }
                """.formatted(overrides);
    }
}
