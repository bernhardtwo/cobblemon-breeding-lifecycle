package io.github.bernhardtwo.breedinglifecycle.mod.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuditLogTest {
    private static final Instant NOW = Instant.parse("2026-10-21T15:00:00Z");

    @TempDir
    Path dir;

    private Path file;
    private AuditLog log;

    @BeforeEach
    void setUp() {
        file = dir.resolve("logs/breeding-lifecycle/audit.jsonl");
        log = new AuditLog(file, () -> true);
    }

    @AfterEach
    void tearDown() {
        log.close();
    }

    @Test
    void writesOneJsonLinePerEntry() throws IOException {
        log.record(entry("lost"));
        log.record(entry("kept"));
        log.flush();
        var lines = Files.readAllLines(file);
        assertThat(lines).hasSize(2);
        assertThat(JsonParser.parseString(lines.get(0))
                        .getAsJsonObject()
                        .get("result")
                        .getAsString())
                .isEqualTo("lost");
    }

    @Test
    void appendsToAnExistingFile() throws IOException {
        log.record(entry("kept"));
        log.flush();
        log.close();
        log = new AuditLog(file, () -> true);
        log.record(entry("lost"));
        log.flush();
        assertThat(Files.readAllLines(file)).hasSize(2);
    }

    @Test
    void entryHasEveryField() {
        assertThat(JsonParser.parseString(entry("kept").toJson())
                        .getAsJsonObject()
                        .keySet())
                .containsExactlyInAnyOrder(
                        "time",
                        "playerUuid",
                        "playerName",
                        "pokemonUuid",
                        "species",
                        "n",
                        "p",
                        "roll",
                        "result",
                        "source");
    }

    @Test
    void entryValuesAreWritten() {
        var pokemon = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var json = JsonParser.parseString(new AuditEntry(
                                NOW,
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "Ash",
                                pokemon,
                                "cobblemon:bulbasaur",
                                3,
                                0.6089,
                                0.25,
                                "lost",
                                "daycare")
                        .toJson())
                .getAsJsonObject();
        assertThat(json.get("playerUuid").getAsString()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(json.get("playerName").getAsString()).isEqualTo("Ash");
        assertThat(json.get("pokemonUuid").getAsString()).isEqualTo(pokemon.toString());
        assertThat(json.get("species").getAsString()).isEqualTo("cobblemon:bulbasaur");
        assertThat(json.get("n").getAsInt()).isEqualTo(3);
        assertThat(json.get("p").getAsDouble()).isEqualTo(0.6089);
        assertThat(json.get("roll").getAsDouble()).isEqualTo(0.25);
        assertThat(json.get("source").getAsString()).isEqualTo("daycare");
    }

    @Test
    void timeIsIsoUtc() {
        assertThat(entryAt(Instant.parse("2026-10-21T15:00:00Z")).toJson())
                .contains("\"time\":\"2026-10-21T15:00:00Z\"");
    }

    @Test
    void auditToleratesMissingOwner() {
        var json = JsonParser.parseString(new AuditEntry(
                                NOW, null, null, UUID.randomUUID(), "cobblemon:ditto", 1, 0.33, 0.5, "kept", "api")
                        .toJson())
                .getAsJsonObject();
        assertThat(json.get("playerUuid").isJsonNull()).isTrue();
        assertThat(json.get("playerName").isJsonNull()).isTrue();
    }

    @Test
    void namesAreEscaped() {
        var json = JsonParser.parseString(new AuditEntry(
                                NOW, UUID.randomUUID(), "a\"b\nc", UUID.randomUUID(), "x", 1, 0.33, 0.5, "kept", "api")
                        .toJson())
                .getAsJsonObject();
        assertThat(json.get("playerName").getAsString()).isEqualTo("a\"b\nc");
    }

    @Test
    void disabledWritesNothing() {
        try (var off = new AuditLog(file, () -> false)) {
            off.record(entry("lost"));
            off.flush();
        }
        assertThat(file).doesNotExist();
    }

    @Test
    void recordWritesOnTheAuditThread() {
        var seen = new AtomicReference<String>();
        try (var threaded = new AuditLog(
                file, () -> true, e -> seen.set(Thread.currentThread().getName()))) {
            threaded.record(entry("kept"));
            threaded.flush();
        }
        assertThat(seen.get()).isEqualTo("breedinglifecycle-audit");
    }

    private static AuditEntry entry(String result) {
        return new AuditEntry(
                NOW, UUID.randomUUID(), "Ash", UUID.randomUUID(), "cobblemon:bulbasaur", 1, 0.33, 0.5, result, "api");
    }

    private static AuditEntry entryAt(Instant time) {
        return new AuditEntry(
                time, UUID.randomUUID(), "Ash", UUID.randomUUID(), "cobblemon:bulbasaur", 1, 0.33, 0.5, "kept", "api");
    }
}
