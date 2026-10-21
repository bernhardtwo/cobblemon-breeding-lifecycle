package io.github.bernhardtwo.breedinglifecycle.mod;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.authlib.GameProfile;
import io.github.bernhardtwo.breedinglifecycle.api.BreedingResult;
import io.github.bernhardtwo.breedinglifecycle.api.ParentResult;
import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import io.github.bernhardtwo.breedinglifecycle.core.LossDecider;
import io.github.bernhardtwo.breedinglifecycle.core.ParentOutcome;
import io.github.bernhardtwo.breedinglifecycle.core.RandomSource;
import io.github.bernhardtwo.breedinglifecycle.core.SeededRandom;
import io.github.bernhardtwo.breedinglifecycle.mod.audit.AuditEntry;
import io.github.bernhardtwo.breedinglifecycle.mod.audit.AuditLog;
import io.github.bernhardtwo.breedinglifecycle.mod.config.ConfigHolder;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Runs a breeding evaluation against the active config, counters and audit log. */
public final class LifecycleService {
    public static final String DITTO = "cobblemon:ditto";

    private static volatile LifecycleService instance;

    private final ConfigHolder config;
    private final AuditLog audit;
    private volatile MinecraftServer server;
    private RandomSource rng = new SeededRandom();

    LifecycleService(ConfigHolder config, AuditLog audit) {
        this.config = Objects.requireNonNull(config, "config");
        this.audit = Objects.requireNonNull(audit, "audit");
    }

    /** The service of the running mod. */
    public static LifecycleService get() {
        LifecycleService current = instance;
        if (current == null) {
            throw new IllegalStateException("Breeding Lifecycle is not initialized");
        }
        return current;
    }

    static void install(LifecycleService service) {
        instance = service;
    }

    void attach(MinecraftServer server) {
        this.server = server;
    }

    /** The active config. */
    public ConfigHolder config() {
        return config;
    }

    /** The audit log. */
    public AuditLog audit() {
        return audit;
    }

    /** Replaces the random source; {@code null} restores an unseeded one. Server thread only. */
    public void setRandomForTesting(RandomSource random) {
        this.rng = random == null ? new SeededRandom() : random;
    }

    /** Rolls both parents, counts the breeding on both and audits it with {@code source}. */
    public BreedingResult evaluate(Pokemon parentA, Pokemon parentB, String source) {
        Objects.requireNonNull(parentA, "parentA");
        Objects.requireNonNull(parentB, "parentB");
        MinecraftServer running = server;
        if (running == null || !running.isSameThread()) {
            throw new IllegalStateException("BreedingLifecycle.evaluate must run on the server thread");
        }
        BreedingCurves curves = config.current().lifecycle().effective();
        ParentResult a = roll(parentA, curves);
        ParentResult b = roll(parentB, curves);
        BreedingCounter.increment(parentA);
        BreedingCounter.increment(parentB);
        Instant now = Instant.now();
        record(running, parentA, a, now, source);
        record(running, parentB, b, now, source);
        return new BreedingResult(a, b);
    }

    /** True when {@code pokemon} is a Ditto and so uses the Ditto curve. */
    public static boolean isDitto(Pokemon pokemon) {
        return DITTO.equals(pokemon.getSpecies().getResourceIdentifier().toString());
    }

    private ParentResult roll(Pokemon pokemon, BreedingCurves curves) {
        ParentOutcome outcome =
                LossDecider.decide(curves.forParent(isDitto(pokemon)), BreedingCounter.get(pokemon), rng);
        return new ParentResult(pokemon.getUuid(), outcome.n(), outcome.p(), outcome.roll(), outcome.survives());
    }

    private void record(MinecraftServer running, Pokemon pokemon, ParentResult result, Instant now, String source) {
        audit.record(entry(
                running,
                pokemon,
                now,
                result.n(),
                result.p(),
                result.roll(),
                result.survives() ? "kept" : "lost",
                source));
    }

    private static AuditEntry entry(
            MinecraftServer running,
            Pokemon pokemon,
            Instant now,
            int n,
            double p,
            double roll,
            String result,
            String source) {
        UUID owner = pokemon.getOwnerUUID();
        return new AuditEntry(
                now,
                owner,
                ownerName(running, owner),
                pokemon.getUuid(),
                pokemon.getSpecies().getResourceIdentifier().toString(),
                n,
                p,
                roll,
                result,
                source);
    }

    private static String ownerName(MinecraftServer running, UUID owner) {
        if (owner == null || running == null) {
            return null;
        }
        ServerPlayer online = running.getPlayerList().getPlayer(owner);
        if (online != null) {
            return online.getGameProfile().getName();
        }
        var cache = running.getProfileCache();
        return cache == null ? null : cache.get(owner).map(GameProfile::getName).orElse(null);
    }
}
