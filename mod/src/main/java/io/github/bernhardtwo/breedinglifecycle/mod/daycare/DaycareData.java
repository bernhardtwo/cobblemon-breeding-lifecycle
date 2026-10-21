package io.github.bernhardtwo.breedinglifecycle.mod.daycare;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The reference daycare of one world: each player's deposited parents and pending offspring. */
public final class DaycareData extends SavedData {
    /** File name under the overworld's {@code data} folder. */
    public static final String NAME = "breedinglifecycle_daycare";

    private static final Logger LOGGER = LoggerFactory.getLogger("breedinglifecycle");
    private static final String PLAYERS = "players";
    private static final String PARENTS = "parents";
    private static final String OFFSPRING = "offspring";

    private final Map<UUID, Entry> players = new HashMap<>();

    /** The world's daycare, created empty on first use. */
    public static DaycareData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(DaycareData::new, DaycareData::load, null), NAME);
    }

    /** Mutable list of the parents {@code player} deposited; whoever changes it calls {@link #setDirty()}. */
    public List<Pokemon> parents(UUID player) {
        return entry(player).parents;
    }

    /** Mutable list of the offspring waiting for {@code player}; whoever changes it calls {@link #setDirty()}. */
    public List<Pokemon> offspring(UUID player) {
        return entry(player).offspring;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        var all = new CompoundTag();
        players.forEach((player, entry) -> {
            if (entry.parents.isEmpty() && entry.offspring.isEmpty()) {
                return;
            }
            var own = new CompoundTag();
            own.put(PARENTS, write(entry.parents, ops));
            own.put(OFFSPRING, write(entry.offspring, ops));
            all.put(player.toString(), own);
        });
        tag.put(PLAYERS, all);
        return tag;
    }

    /** Reads what {@link #save} wrote; missing lists load empty, and a Pokémon that fails to load is logged and skipped. */
    public static DaycareData load(CompoundTag tag, HolderLookup.Provider registries) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        var data = new DaycareData();
        CompoundTag all = tag.getCompound(PLAYERS);
        for (String key : all.getAllKeys()) {
            UUID player;
            try {
                player = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Skipping daycare entry with an invalid player id {}", key);
                continue;
            }
            CompoundTag own = all.getCompound(key);
            read(own.getList(PARENTS, Tag.TAG_COMPOUND), ops, data.parents(player));
            read(own.getList(OFFSPRING, Tag.TAG_COMPOUND), ops, data.offspring(player));
        }
        return data;
    }

    private Entry entry(UUID player) {
        return players.computeIfAbsent(player, ignored -> new Entry());
    }

    private static ListTag write(List<Pokemon> pokemon, DynamicOps<Tag> ops) {
        var list = new ListTag();
        for (Pokemon each : pokemon) {
            list.add(Pokemon.Companion.getCODEC().encodeStart(ops, each).getOrThrow());
        }
        return list;
    }

    private static void read(ListTag list, DynamicOps<Tag> ops, List<Pokemon> into) {
        for (int i = 0; i < list.size(); i++) {
            Tag each = list.get(i);
            Pokemon.Companion.getCODEC()
                    .parse(ops, each)
                    .resultOrPartial(error -> LOGGER.error("Skipping a daycare Pokémon that failed to load: {}", error))
                    .ifPresent(into::add);
        }
    }

    private static final class Entry {
        private final List<Pokemon> parents = new ArrayList<>();
        private final List<Pokemon> offspring = new ArrayList<>();
    }
}
