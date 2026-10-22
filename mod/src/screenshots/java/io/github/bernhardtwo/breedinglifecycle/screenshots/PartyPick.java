package io.github.bernhardtwo.breedinglifecycle.screenshots;

import java.util.List;
import java.util.UUID;

/** Finds the party slot of a Pokémon the screenshots run is tracking. */
public final class PartyPick {
    private PartyPick() {}

    /** The 1-based slot of {@code wanted} in {@code slots}, where empty slots are {@code null}. */
    public static int slotOf(List<UUID> slots, UUID wanted) {
        int index = slots.indexOf(wanted);
        if (index < 0) {
            throw new IllegalStateException("Pokémon " + wanted + " is not in the party");
        }
        return index + 1;
    }
}
