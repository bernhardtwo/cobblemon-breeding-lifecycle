package io.github.bernhardtwo.breedinglifecycle.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MessagesTest {
    @Test
    void resolvesSpanishVariantsToSpanish() {
        assertThat(Messages.resolve(null, Locale.forLanguageTag("es-MX")).getLanguage())
                .isEqualTo("es");
        assertThat(Messages.resolve(null, Locale.forLanguageTag("pt-BR")).getLanguage())
                .isEqualTo("en");
    }

    @Test
    void explicitLanguageWins() {
        assertThat(Messages.resolve("en", Locale.forLanguageTag("es-AR")).getLanguage())
                .isEqualTo("en");
    }

    @Test
    void unknownLanguageIsRejected() {
        assertThatThrownBy(() -> Messages.resolve("fr", Locale.ENGLISH)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bundlesHaveSameKeys() throws IOException {
        assertThat(keys("messages_es")).isEqualTo(keys("messages_en"));
    }

    private static Set<String> keys(String bundle) throws IOException {
        var properties = new Properties();
        try (InputStream in = MessagesTest.class.getResourceAsStream("/" + bundle + ".properties")) {
            properties.load(in);
        }
        return properties.stringPropertyNames();
    }
}
