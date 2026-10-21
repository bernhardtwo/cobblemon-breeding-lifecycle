package io.github.bernhardtwo.breedinglifecycle.mod;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** The breedings done by a Pokémon, kept in its persistent data so it is saved and traded with it. */
public final class BreedingCounter {
    public static final String KEY = "breedinglifecycle";
    public static final String BREEDINGS = "breedings";

    private BreedingCounter() {}

    /** Breedings done so far; 0 when absent, not a number or negative. Any numeric tag counts, since JSON storage narrows it. */
    public static int get(Pokemon pokemon) {
        CompoundTag data = pokemon.getPersistentData();
        if (!data.contains(KEY, Tag.TAG_COMPOUND)) {
            return 0;
        }
        CompoundTag own = data.getCompound(KEY);
        if (!own.contains(BREEDINGS, Tag.TAG_ANY_NUMERIC)) {
            return 0;
        }
        return (int) Math.clamp(own.getLong(BREEDINGS), 0, Integer.MAX_VALUE);
    }

    /** Adds one breeding in place and marks the Pokémon changed so its store saves it. */
    public static void increment(Pokemon pokemon) {
        int next = get(pokemon) == Integer.MAX_VALUE ? Integer.MAX_VALUE : get(pokemon) + 1;
        CompoundTag data = pokemon.getPersistentData();
        if (!data.contains(KEY, Tag.TAG_COMPOUND)) {
            data.put(KEY, new CompoundTag());
        }
        data.getCompound(KEY).putInt(BREEDINGS, next);
        pokemon.onChange(null);
    }
}
