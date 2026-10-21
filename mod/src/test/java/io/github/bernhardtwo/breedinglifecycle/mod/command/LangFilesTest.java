package io.github.bernhardtwo.breedinglifecycle.mod.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LangFilesTest {
    @Test
    void spanishAndEnglishHaveSameKeys() throws IOException {
        assertThat(keys("es_es")).isEqualTo(keys("en_us"));
    }

    @Test
    void everyKeyIsNamespaced() throws IOException {
        assertThat(keys("en_us")).isNotEmpty().allMatch(k -> k.startsWith("breedinglifecycle."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"es_ar", "es_cl", "es_ec", "es_mx", "es_uy", "es_ve"})
    void spanishVariantsShipTheSpanishText(String locale) throws IOException {
        assertThat(text(locale)).isEqualTo(text("es_es"));
    }

    private static Set<String> keys(String locale) throws IOException {
        return new TreeSet<>(
                JsonParser.parseString(text(locale)).getAsJsonObject().keySet());
    }

    private static String text(String locale) throws IOException {
        String path = "/data/breedinglifecycle/lang/" + locale + ".json";
        var stream = LangFilesTest.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("lang file not found: " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).toString();
        }
    }
}
