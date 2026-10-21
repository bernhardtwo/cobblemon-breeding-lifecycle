package io.github.bernhardtwo.breedinglifecycle.mod.audit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.UUID;

/** One audited parent roll; {@code playerUuid} and {@code playerName} are null when the owner is unknown. */
public record AuditEntry(
        Instant time,
        UUID playerUuid,
        String playerName,
        UUID pokemonUuid,
        String species,
        int n,
        double p,
        double roll,
        String result,
        String source) {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    /** One JSON object on a single line. */
    public String toJson() {
        var json = new JsonObject();
        json.addProperty("time", time.toString());
        json.addProperty("playerUuid", playerUuid == null ? null : playerUuid.toString());
        json.addProperty("playerName", playerName);
        json.addProperty("pokemonUuid", pokemonUuid == null ? null : pokemonUuid.toString());
        json.addProperty("species", species);
        json.addProperty("n", n);
        json.addProperty("p", p);
        json.addProperty("roll", roll);
        json.addProperty("result", result);
        json.addProperty("source", source);
        return GSON.toJson(json);
    }
}
