package io.github.bernhardtwo.breedinglifecycle.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CurveVectorsTest {
    static Stream<JsonObject> cases() throws IOException {
        var file = Path.of(System.getProperty("curveVectors"));
        var root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        return root.getAsJsonArray("cases").asList().stream().map(c -> c.getAsJsonObject());
    }

    @ParameterizedTest
    @MethodSource("cases")
    void matchesSharedVector(JsonObject vector) {
        var spec = vector.getAsJsonObject("curve");
        var max = spec.get("maxBreedings").isJsonNull()
                ? null
                : spec.get("maxBreedings").getAsInt();
        var curve = new Curve(spec.get("p1").getAsDouble(), spec.get("r").getAsDouble(), max);
        var p = vector.getAsJsonArray("p");
        var s = vector.getAsJsonArray("S");
        for (int n = 1; n <= p.size(); n++) {
            assertThat(curve.lossProbability(n)).isCloseTo(p.get(n - 1).getAsDouble(), within(1e-9));
            assertThat(curve.survival(n)).isCloseTo(s.get(n - 1).getAsDouble(), within(1e-9));
        }
        assertThat(curve.expectedOffspring()).isCloseTo(vector.get("E").getAsDouble(), within(1e-9));
    }
}
