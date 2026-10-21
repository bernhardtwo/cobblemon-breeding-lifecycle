package io.github.bernhardtwo.breedinglifecycle.mod.adapter;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.CollectEggEvent;
import com.cobblemon.mod.common.api.storage.StoreCoordinates;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingResult;
import io.github.bernhardtwo.breedinglifecycle.api.ParentResult;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Applies the loss to {@code COLLECT_EGG} from breeding mods, when enabled and both parents are in a real store. */
public final class EggEventAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("breedinglifecycle");
    private static final String SOURCE = "egg-event";

    private EggEventAdapter() {}

    /** Subscribes once; the {@code eggEventAdapter} switch is read on every event, so a reload turns it on or off. */
    public static void register() {
        CobblemonEvents.COLLECT_EGG.subscribe(Priority.NORMAL, event -> {
            handle(event);
        });
    }

    private static void handle(CollectEggEvent event) {
        LifecycleService service = LifecycleService.get();
        if (!service.config().current().eggEventAdapter()) {
            return;
        }
        Pokemon male = event.getMaleParent();
        Pokemon female = event.getFemaleParent();
        if (!inPartyOrPc(male) || !inPartyOrPc(female)) {
            LOGGER.warn(
                    "COLLECT_EGG for {} with parents outside any party or PC; ignored",
                    event.getPlayer().getGameProfile().getName());
            return;
        }
        BreedingResult result = service.evaluate(male, female, SOURCE);
        retireIfLost(male, result.parentA());
        retireIfLost(female, result.parentB());
    }

    private static boolean inPartyOrPc(Pokemon pokemon) {
        StoreCoordinates<?> coordinates = pokemon.getStoreCoordinates().get();
        return coordinates != null
                && coordinates.get() == pokemon
                && (coordinates.getStore() instanceof PartyStore || coordinates.getStore() instanceof PCStore);
    }

    private static void retireIfLost(Pokemon pokemon, ParentResult result) {
        if (result.survives()) {
            return;
        }
        StoreCoordinates<?> coordinates = pokemon.getStoreCoordinates().get();
        if (coordinates == null) {
            return;
        }
        if (pokemon.getTetheringId() != null) {
            pokemon.setTetheringId(null);
        }
        if (!coordinates.getStore().remove(pokemon)) {
            LOGGER.warn("Could not remove lost parent {} from its store", pokemon.getUuid());
        }
    }
}
