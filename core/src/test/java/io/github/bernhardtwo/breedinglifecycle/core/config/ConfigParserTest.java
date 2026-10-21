package io.github.bernhardtwo.breedinglifecycle.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigParserTest {
    @Test
    void defaultConfigParsesToDefaultCurves() {
        assertThat(valid(DefaultConfig.json()).effective()).isEqualTo(BreedingCurves.DEFAULT);
    }

    @Test
    void overrideReplacesOnlyThatField() {
        var config = valid(withOverrides("{\"r\":0.9}"));
        assertThat(config.effective().main()).isEqualTo(new Curve(0.33, 0.9, null));
        assertThat(config.presets().get("default")).isEqualTo(BreedingCurves.DEFAULT);
    }

    @Test
    void dittoOverrideMergesFieldByField() {
        assertThat(valid(withOverrides("{\"ditto\":{\"p1\":0.5}}")).effective().ditto())
                .isEqualTo(new Curve(0.5, 0.764, null));
    }

    @Test
    void explicitNullOverrideRemovesLimit() {
        assertThat(valid(presetMax(5, "{\"maxBreedings\":null}"))
                        .effective()
                        .main()
                        .maxBreedings())
                .isNull();
    }

    @Test
    void absentOverrideInheritsLimit() {
        assertThat(valid(presetMax(5, "{}")).effective().main().maxBreedings()).isEqualTo(5);
    }

    @Test
    void rangeErrorNamesFieldValueAndRange() {
        assertThat(errors(dittoR(1.2))).containsExactly("presets.default.ditto.r = 1.2: must be in (0, 1]");
    }

    @Test
    void rejectsNonIntegerMaxBreedings() {
        assertThat(errors(withOverrides("{\"maxBreedings\":2.5}")))
                .singleElement()
                .asString()
                .startsWith("overrides.maxBreedings = 2.5");
    }

    @Test
    void rejectsNonNumericP1() {
        assertThat(errors(withOverrides("{\"p1\":\"high\"}")))
                .singleElement()
                .asString()
                .contains("must be a number");
    }

    @Test
    void unknownPresetRejected() {
        assertThat(errors("{\"preset\":\"nope\",\"presets\":{}}"))
                .singleElement()
                .asString()
                .contains("preset = nope");
    }

    @Test
    void neverLosingCurveRejected() {
        assertThat(errors(withOverrides("{\"p1\":0,\"r\":1}")))
                .singleElement()
                .asString()
                .contains("maxBreedings");
    }

    @Test
    void malformedJsonIsInvalidNotThrown() {
        assertThat(ConfigParser.parse("{")).isInstanceOf(ConfigResult.Invalid.class);
    }

    @Test
    void reportsEveryErrorAtOnce() {
        assertThat(errors(withOverrides("{\"p1\":2,\"r\":0}"))).hasSize(2);
    }

    @Test
    void unknownCurveFieldIsRejected() {
        assertThat(errors(withOverrides("{\"p2\":0.5}"))).containsExactly("overrides.p2: unknown field");
    }

    @Test
    void modBlocksAreKeptInRaw() {
        assertThat(valid(DefaultConfig.json())
                        .raw()
                        .getAsJsonObject("audit")
                        .get("enabled")
                        .getAsBoolean())
                .isTrue();
    }

    private static LifecycleConfig valid(String json) {
        var result = ConfigParser.parse(json);
        assertThat(result).isInstanceOf(ConfigResult.Valid.class);
        return ((ConfigResult.Valid) result).config();
    }

    private static List<String> errors(String json) {
        var result = ConfigParser.parse(json);
        assertThat(result).isInstanceOf(ConfigResult.Invalid.class);
        return ((ConfigResult.Invalid) result).errors();
    }

    private static JsonObject defaults() {
        return JsonParser.parseString(DefaultConfig.json()).getAsJsonObject();
    }

    private static String withOverrides(String overrides) {
        var root = defaults();
        root.add("overrides", JsonParser.parseString(overrides));
        return root.toString();
    }

    private static String presetMax(int max, String overrides) {
        var root = JsonParser.parseString(withOverrides(overrides)).getAsJsonObject();
        root.getAsJsonObject("presets").getAsJsonObject("default").addProperty("maxBreedings", max);
        return root.toString();
    }

    private static String dittoR(double r) {
        var root = defaults();
        root.getAsJsonObject("presets")
                .getAsJsonObject("default")
                .getAsJsonObject("ditto")
                .addProperty("r", r);
        return root.toString();
    }
}
