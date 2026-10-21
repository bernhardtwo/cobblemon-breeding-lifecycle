package io.github.bernhardtwo.breedinglifecycle.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CliTest {
    @TempDir
    Path dir;

    private String lastErr = "";

    @Test
    void curveEnglishShowsBaseline() {
        var out = run("curve", "--lang", "en");
        assertThat(out).contains("0.4881").contains("0.6700").contains("E = 2.1981");
        assertThat(out.strip()).endsWith("E = 2.1981");
    }

    @Test
    void curveSpanishTranslatesHeaders() {
        assertThat(run("curve", "--lang", "es")).contains("E = 2,1981").contains("Curva");
    }

    @Test
    void simulateIsReproducible() {
        assertThat(run("simulate", "--parents", "5000", "--seed", "3", "--lang", "en"))
                .isEqualTo(run("simulate", "--parents", "5000", "--seed", "3", "--lang", "en"));
    }

    @Test
    void simulatePrintsTheSeedItUsed() {
        assertThat(run("simulate", "--parents", "100", "--lang", "en")).containsPattern("seed -?\\d+");
    }

    @Test
    void compareShowsCurrentSystem() {
        assertThat(run("compare", "--lang", "en")).contains("1.0000").contains("2.1981");
    }

    @Test
    void dittoFlagUsesDittoCurve() throws IOException {
        assertThat(run("curve", "--ditto", "--config", cfgWithDittoP1(0.5), "--lang", "en"))
                .contains("0.5000");
    }

    @Test
    void invalidConfigExitsWithErrors() throws IOException {
        assertThat(exitCode("curve", "--config", cfgWithR(0), "--lang", "en")).isEqualTo(2);
        assertThat(lastErr).contains("must be in (0, 1]");
    }

    @Test
    void rejectsNonPositiveParents() {
        assertThat(exitCode("simulate", "--parents", "0", "--lang", "en")).isEqualTo(2);
    }

    @Test
    void rejectsUnknownLanguage() {
        assertThat(exitCode("curve", "--lang", "fr")).isEqualTo(2);
    }

    @Test
    void startsOnASystemThatIsNeitherSpanishNorEnglish() {
        Locale previous = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        try {
            assertThat(run("curve")).contains("Expected offspring per parent");
        } finally {
            Locale.setDefault(previous);
        }
    }

    private String run(String... args) {
        var out = new StringWriter();
        assertThat(execute(out, args)).isZero();
        return out.toString();
    }

    private int exitCode(String... args) {
        return execute(new StringWriter(), args);
    }

    private int execute(StringWriter out, String... args) {
        var err = new StringWriter();
        var cli = Main.commandLine();
        cli.setOut(new PrintWriter(out));
        cli.setErr(new PrintWriter(err));
        int code = cli.execute(args);
        lastErr = err.toString();
        return code;
    }

    private String cfgWithDittoP1(double p1) throws IOException {
        return write("{\"presets\":{\"default\":{\"p1\":0.33,\"r\":0.764,\"ditto\":{\"p1\":" + p1 + ",\"r\":0.764}}}}");
    }

    private String cfgWithR(double r) throws IOException {
        return write("{\"presets\":{\"default\":{\"p1\":0.33,\"r\":" + r + "}}}");
    }

    private String write(String json) throws IOException {
        var file = dir.resolve("config.json");
        Files.writeString(file, json);
        return file.toString();
    }
}
