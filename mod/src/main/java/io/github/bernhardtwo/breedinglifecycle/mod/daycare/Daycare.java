package io.github.bernhardtwo.breedinglifecycle.mod.daycare;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingResult;
import io.github.bernhardtwo.breedinglifecycle.api.ParentResult;
import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/** The reference daycare: two slots per player, breeding done in one call against one {@link DaycareData}. */
public final class Daycare {
    /** Audit source of breedings done here. */
    public static final String SOURCE = "daycare";

    private static final String KEY = "breedinglifecycle.daycare.";
    private static final int SLOTS = 2;
    private static final int PARTY_SIZE = 6;

    private Daycare() {}

    /** Moves the Pokémon in party {@code slot} (1-6) into the daycare. */
    public static Component deposit(ServerPlayer player, int slot) {
        if (!enabled()) {
            return text("disabled");
        }
        if (slot < 1 || slot > PARTY_SIZE) {
            return text("empty_slot", slot);
        }
        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            return text("in_battle");
        }
        PlayerPartyStore party = party(player);
        Pokemon pokemon = party.get(slot - 1);
        if (pokemon == null) {
            return text("empty_slot", slot);
        }
        DaycareData data = data(player);
        List<Pokemon> parents = data.parents(player.getUUID());
        if (parents.size() >= SLOTS) {
            return text("full");
        }
        if (pokemon.getEntity() != null) {
            pokemon.recall();
        }
        if (!party.remove(pokemon)) {
            return text("deposit_failed", name(pokemon));
        }
        parents.add(pokemon);
        data.setDirty();
        return text("deposited", name(pokemon));
    }

    /**
     * Breeds the two deposited parents on the server thread: creates the offspring, rolls both parents, drops the lost
     * ones and keeps the offspring, all in {@link DaycareData} and marked dirty once.
     */
    public static BreedOutcome breed(ServerPlayer player) {
        if (!enabled()) {
            return new BreedOutcome.Rejected(KEY + "disabled");
        }
        if (!player.getServer().isSameThread()) {
            throw new IllegalStateException("The daycare breeds on the server thread only");
        }
        DaycareData data = data(player);
        List<Pokemon> parents = data.parents(player.getUUID());
        if (parents.size() < SLOTS) {
            return new BreedOutcome.Rejected(KEY + "need_two");
        }
        Pokemon a = parents.get(0);
        Pokemon b = parents.get(1);
        Optional<String> rejection = Compatibility.rejection(a, b);
        if (rejection.isPresent()) {
            return new BreedOutcome.Rejected(rejection.get());
        }
        Pokemon offspring = Offspring.of(a, b);
        BreedingResult result = LifecycleService.get().evaluate(a, b, SOURCE, player.getUUID());
        if (!result.parentA().survives()) {
            parents.remove(a);
        }
        if (!result.parentB().survives()) {
            parents.remove(b);
        }
        data.offspring(player.getUUID()).add(offspring);
        data.setDirty();
        return new BreedOutcome.Bred(offspring, result);
    }

    /** The parents with their counters and next loss chance, and the pending offspring. */
    public static Component status(ServerPlayer player) {
        DaycareData data = data(player);
        List<Pokemon> parents = data.parents(player.getUUID());
        List<Pokemon> offspring = data.offspring(player.getUUID());
        if (parents.isEmpty() && offspring.isEmpty()) {
            return text("status.empty");
        }
        List<Component> lines = new ArrayList<>();
        for (Pokemon parent : parents) {
            int breedings = BreedingCounter.get(parent);
            lines.add(text("status.parent", name(parent), breedings, decimal(nextLossChance(parent, breedings))));
        }
        if (!offspring.isEmpty()) {
            lines.add(text("status.offspring", offspring.size(), names(offspring)));
        }
        return join(lines);
    }

    /** Returns parents and offspring to the party, or to the PC when the party is full; what fits nowhere stays. */
    public static Component withdraw(ServerPlayer player) {
        DaycareData data = data(player);
        List<Pokemon> parents = data.parents(player.getUUID());
        List<Pokemon> offspring = data.offspring(player.getUUID());
        if (parents.isEmpty() && offspring.isEmpty()) {
            return text("status.empty");
        }
        int returned = giveBack(player, parents) + giveBack(player, offspring);
        if (returned > 0) {
            data.setDirty();
        }
        int left = parents.size() + offspring.size();
        return left == 0 ? text("withdrawn", returned) : text("withdrawn_partly", returned, left);
    }

    /** The chat lines that describe {@code outcome}. */
    public static Component describe(BreedOutcome outcome) {
        return switch (outcome) {
            case BreedOutcome.Rejected rejected -> Component.translatable(rejected.reasonKey());
            case BreedOutcome.Bred bred ->
                join(List.of(
                        text("bred", name(bred.offspring())),
                        parentLine(1, bred.result().parentA()),
                        parentLine(2, bred.result().parentB())));
        };
    }

    private static Component parentLine(int index, ParentResult parent) {
        String key = parent.survives() ? "parent.kept" : "parent.lost";
        return text(key, index, parent.n(), decimal(parent.p()), decimal(parent.roll()));
    }

    private static int giveBack(ServerPlayer player, List<Pokemon> pokemon) {
        PlayerPartyStore party = party(player);
        PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        int returned = 0;
        for (Pokemon each : List.copyOf(pokemon)) {
            if (party.add(each) || pc.add(each)) {
                pokemon.remove(each);
                returned++;
            }
        }
        return returned;
    }

    private static double nextLossChance(Pokemon pokemon, int breedings) {
        Curve curve = LifecycleService.get()
                .config()
                .current()
                .lifecycle()
                .effective()
                .forParent(LifecycleService.isDitto(pokemon));
        return curve.lossProbability(breedings == Integer.MAX_VALUE ? breedings : breedings + 1);
    }

    private static boolean enabled() {
        return LifecycleService.get().config().current().referenceDaycare();
    }

    private static PlayerPartyStore party(ServerPlayer player) {
        return Cobblemon.INSTANCE.getStorage().getParty(player);
    }

    private static DaycareData data(ServerPlayer player) {
        return DaycareData.get(player.getServer());
    }

    private static Component name(Pokemon pokemon) {
        return pokemon.getSpecies().getTranslatedName();
    }

    private static Component names(List<Pokemon> pokemon) {
        MutableComponent all = Component.empty();
        for (int i = 0; i < pokemon.size(); i++) {
            if (i > 0) {
                all.append(", ");
            }
            all.append(name(pokemon.get(i)));
        }
        return all;
    }

    private static Component join(List<Component> lines) {
        MutableComponent all = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                all.append("\n");
            }
            all.append(lines.get(i));
        }
        return all;
    }

    private static MutableComponent text(String key, Object... args) {
        return Component.translatable(KEY + key, args);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
