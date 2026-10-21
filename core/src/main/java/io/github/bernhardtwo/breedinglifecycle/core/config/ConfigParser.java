package io.github.bernhardtwo.breedinglifecycle.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Parses and validates the JSON config shared by the CLI and the mod. */
public final class ConfigParser {
    private static final String DEFAULT_PRESET = "default";
    private static final Set<String> CURVE_FIELDS = Set.of("p1", "r", "maxBreedings");

    private ConfigParser() {}

    /** Parses {@code json}; never throws on bad input, returning {@link ConfigResult.Invalid} instead. */
    public static ConfigResult parse(String json) {
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (JsonParseException e) {
            return new ConfigResult.Invalid(List.of("invalid JSON: " + e.getMessage()));
        }
        if (root == null || !root.isJsonObject()) {
            return new ConfigResult.Invalid(List.of("config: must be a JSON object"));
        }
        return new ConfigParser.Run(root.getAsJsonObject()).parse();
    }

    private static final class Run {
        private final JsonObject root;
        private final List<String> errors = new ArrayList<>();

        Run(JsonObject root) {
            this.root = root;
        }

        ConfigResult parse() {
            String preset = presetName();
            Map<String, CurveSpec> specs = presetSpecs();
            CurveSpec overrides = overrides();
            if (preset != null && specs != null && !specs.containsKey(preset)) {
                errors.add("preset = " + preset + ": no such preset in presets");
            }
            if (!errors.isEmpty()) {
                return new ConfigResult.Invalid(errors);
            }
            var presets = new LinkedHashMap<String, BreedingCurves>();
            specs.forEach((name, spec) -> presets.put(name, spec.toCurves()));
            CurveSpec effective = specs.get(preset).overriddenBy(overrides);
            checkNeverLost("overrides", effective);
            checkNeverLost("overrides.ditto", effective.ditto());
            if (!errors.isEmpty()) {
                return new ConfigResult.Invalid(errors);
            }
            return new ConfigResult.Valid(new LifecycleConfig(preset, presets, effective.toCurves(), root));
        }

        private String presetName() {
            JsonElement value = root.get("preset");
            if (value == null) {
                return DEFAULT_PRESET;
            }
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                errors.add("preset = " + value + ": must be a string");
                return null;
            }
            return value.getAsString();
        }

        private Map<String, CurveSpec> presetSpecs() {
            JsonElement value = root.get("presets");
            if (value == null || !value.isJsonObject()) {
                errors.add("presets: must be an object of named curves");
                return null;
            }
            var specs = new LinkedHashMap<String, CurveSpec>();
            for (var entry : value.getAsJsonObject().entrySet()) {
                String path = "presets." + entry.getKey();
                if (!entry.getValue().isJsonObject()) {
                    errors.add(path + ": must be an object");
                    continue;
                }
                CurveSpec spec = curveSpec(path, entry.getValue().getAsJsonObject(), true);
                requireField(path, spec.p1, "p1");
                requireField(path, spec.r, "r");
                CurveSpec ditto = spec.ditto() == null ? spec.withoutDitto() : spec.ditto();
                requireField(path + ".ditto", ditto.p1, "p1");
                requireField(path + ".ditto", ditto.r, "r");
                CurveSpec complete = spec.withDitto(ditto);
                if (complete.isComplete()) {
                    checkNeverLost(path, complete);
                    checkNeverLost(path + ".ditto", ditto);
                }
                specs.put(entry.getKey(), complete);
            }
            return specs;
        }

        private CurveSpec overrides() {
            JsonElement value = root.get("overrides");
            if (value == null || value.isJsonNull()) {
                return CurveSpec.EMPTY;
            }
            if (!value.isJsonObject()) {
                errors.add("overrides: must be an object");
                return CurveSpec.EMPTY;
            }
            return curveSpec("overrides", value.getAsJsonObject(), true);
        }

        private CurveSpec curveSpec(String path, JsonObject object, boolean allowDitto) {
            Double p1 = null;
            Double r = null;
            boolean hasMax = false;
            Integer max = null;
            CurveSpec ditto = null;
            for (var entry : object.entrySet()) {
                String field = path + "." + entry.getKey();
                JsonElement value = entry.getValue();
                switch (entry.getKey()) {
                    case "p1" -> p1 = number(field, value, 0, true);
                    case "r" -> r = number(field, value, 0, false);
                    case "maxBreedings" -> {
                        hasMax = true;
                        max = maxBreedings(field, value);
                    }
                    case "ditto" -> {
                        if (allowDitto && value.isJsonObject()) {
                            ditto = curveSpec(field, value.getAsJsonObject(), false);
                        } else {
                            errors.add(field + (allowDitto ? ": must be an object" : ": unknown field"));
                        }
                    }
                    default -> errors.add(field + ": unknown field");
                }
            }
            return new CurveSpec(p1, r, hasMax, max, ditto);
        }

        private Double number(String field, JsonElement value, double lower, boolean lowerInclusive) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                errors.add(field + " = " + value + ": must be a number");
                return null;
            }
            double number = value.getAsDouble();
            boolean aboveLower = lowerInclusive ? number >= lower : number > lower;
            if (!(aboveLower && number <= 1)) {
                errors.add(field + " = " + value + ": must be in " + (lowerInclusive ? "[" : "(") + "0, 1]");
                return null;
            }
            return number;
        }

        private Integer maxBreedings(String field, JsonElement value) {
            if (value.isJsonNull()) {
                return null;
            }
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                BigDecimal number = value.getAsBigDecimal();
                if (number.stripTrailingZeros().scale() <= 0
                        && number.compareTo(BigDecimal.ONE) >= 0
                        && number.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {
                    return number.intValueExact();
                }
            }
            errors.add(field + " = " + value + ": must be null or an integer >= 1");
            return null;
        }

        private void requireField(String path, Double value, String name) {
            if (value == null && errors.stream().noneMatch(e -> e.startsWith(path + "." + name + " "))) {
                errors.add(path + "." + name + ": required");
            }
        }

        private void checkNeverLost(String path, CurveSpec spec) {
            if (spec.isComplete()) {
                Curve.validate(spec.p1, spec.r, spec.max).forEach(e -> errors.add(path + ": " + e));
            }
        }
    }

    private record CurveSpec(Double p1, Double r, boolean hasMax, Integer max, CurveSpec ditto) {
        static final CurveSpec EMPTY = new CurveSpec(null, null, false, null, null);

        boolean isComplete() {
            return p1 != null && r != null;
        }

        CurveSpec withoutDitto() {
            return new CurveSpec(p1, r, hasMax, max, null);
        }

        CurveSpec withDitto(CurveSpec newDitto) {
            return new CurveSpec(p1, r, hasMax, max, newDitto);
        }

        CurveSpec overriddenBy(CurveSpec o) {
            CurveSpec mergedDitto = ditto.mergeFields(o.ditto == null ? EMPTY : o.ditto);
            return mergeFields(o).withDitto(mergedDitto);
        }

        private CurveSpec mergeFields(CurveSpec o) {
            return new CurveSpec(
                    o.p1 != null ? o.p1 : p1, o.r != null ? o.r : r, hasMax || o.hasMax, o.hasMax ? o.max : max, ditto);
        }

        BreedingCurves toCurves() {
            return new BreedingCurves(new Curve(p1, r, max), new Curve(ditto.p1, ditto.r, ditto.max));
        }
    }
}
