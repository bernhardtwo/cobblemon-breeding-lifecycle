package io.github.bernhardtwo.breedinglifecycle.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class WebExportTest {
    @Test
    void webExportExampleIsAValidConfig() throws IOException {
        assertThat(ConfigParser.parse(Files.readString(Path.of(System.getProperty("webExportExample")))))
                .isInstanceOf(ConfigResult.Valid.class);
    }
}
