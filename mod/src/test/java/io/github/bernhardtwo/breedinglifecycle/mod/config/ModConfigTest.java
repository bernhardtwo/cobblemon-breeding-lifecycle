package io.github.bernhardtwo.breedinglifecycle.mod.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import io.github.bernhardtwo.breedinglifecycle.core.config.DefaultConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModConfigTest {
    @TempDir
    Path dir;

    private Path file;

    @BeforeEach
    void setUp() {
        file = dir.resolve("breeding-lifecycle.json");
    }

    @Test
    void missingFileIsCreatedWithDefaults() throws IOException {
        var r = (ModConfig.Result.Loaded) ModConfig.load(dir.resolve("c.json"));
        assertThat(r.created()).isTrue();
        assertThat(Files.readString(dir.resolve("c.json"))).isEqualTo(DefaultConfig.json());
        assertThat(r.settings().audit()).isTrue();
        assertThat(r.settings().referenceDaycare()).isFalse();
    }

    @Test
    void existingFileIsNotReportedAsCreated() throws IOException {
        write(DefaultConfig.json());
        assertThat(loaded().created()).isFalse();
    }

    @Test
    void missingSwitchUsesDefault() throws IOException {
        write("{\"presets\":{\"default\":{\"p1\":0.33,\"r\":0.764}}}");
        assertThat(loaded().settings().audit()).isTrue();
        assertThat(loaded().settings().eggEventAdapter()).isFalse();
        assertThat(loaded().settings().referenceDaycare()).isFalse();
    }

    @Test
    void switchesAreRead() throws IOException {
        write(
                defaultsWith(
                        "\"audit\":{\"enabled\":false},\"eggEventAdapter\":{\"enabled\":true},\"referenceDaycare\":{\"enabled\":true}"));
        var settings = loaded().settings();
        assertThat(settings.audit()).isFalse();
        assertThat(settings.eggEventAdapter()).isTrue();
        assertThat(settings.referenceDaycare()).isTrue();
    }

    @Test
    void rejectsNonBooleanSwitch() throws IOException {
        write(defaultsWith("\"audit\":{\"enabled\":\"yes\"}"));
        assertThat(rejected().errors()).containsExactly("audit.enabled = \"yes\": must be true or false");
    }

    @Test
    void rejectsSwitchBlockThatIsNotAnObject() throws IOException {
        write(defaultsWith("\"eggEventAdapter\":true"));
        assertThat(rejected().errors()).containsExactly("eggEventAdapter = true: must be an object");
    }

    @Test
    void reportsCurveAndSwitchErrorsTogether() throws IOException {
        write(defaultsWith("\"overrides\":{\"r\":0},\"audit\":{\"enabled\":1}"));
        assertThat(rejected().errors()).hasSize(2).anyMatch(e -> e.startsWith("overrides.r"));
    }

    @Test
    void reloadKeepsPreviousOnInvalidFile() throws IOException {
        var h = ConfigHolder.open(file);
        write(defaultsWith("\"overrides\":{\"r\":0}"));
        assertThat(h.reload()).singleElement().asString().contains("overrides.r");
        assertThat(h.current().lifecycle().effective()).isEqualTo(BreedingCurves.DEFAULT);
    }

    @Test
    void reloadSwapsOnValidFile() throws IOException {
        var h = ConfigHolder.open(file);
        write(defaultsWith("\"overrides\":{\"r\":0.9}"));
        assertThat(h.reload()).isEmpty();
        assertThat(h.current().lifecycle().effective().main().r()).isEqualTo(0.9);
    }

    @Test
    void openFallsBackToDefaultsWhenFileIsInvalid() throws IOException {
        write("{");
        assertThat(ConfigHolder.open(file).current().lifecycle().effective()).isEqualTo(BreedingCurves.DEFAULT);
    }

    @Test
    void openFallsBackToDefaultSwitchesWhenFileIsInvalid() throws IOException {
        write("{");
        var settings = ConfigHolder.open(file).current();
        assertThat(settings.audit()).isTrue();
        assertThat(settings.eggEventAdapter()).isFalse();
    }

    private void write(String json) throws IOException {
        Files.writeString(file, json);
    }

    private static String defaultsWith(String fields) {
        JsonObject root = JsonParser.parseString(DefaultConfig.json()).getAsJsonObject();
        JsonParser.parseString("{" + fields + "}")
                .getAsJsonObject()
                .entrySet()
                .forEach(e -> root.add(e.getKey(), e.getValue()));
        return root.toString();
    }

    private ModConfig.Result.Loaded loaded() {
        return (ModConfig.Result.Loaded) ModConfig.load(file);
    }

    private ModConfig.Result.Rejected rejected() {
        return (ModConfig.Result.Rejected) ModConfig.load(file);
    }
}
