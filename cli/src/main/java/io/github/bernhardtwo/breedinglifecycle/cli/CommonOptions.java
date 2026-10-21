package io.github.bernhardtwo.breedinglifecycle.cli;

import io.github.bernhardtwo.breedinglifecycle.core.BreedingCurves;
import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import io.github.bernhardtwo.breedinglifecycle.core.config.ConfigParser;
import io.github.bernhardtwo.breedinglifecycle.core.config.ConfigResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/** Options every command shares: language, config file and the Ditto switch. */
final class CommonOptions {
    @Spec(Spec.Target.MIXEE)
    CommandSpec spec;

    @Option(names = "--lang", paramLabel = "es|en", descriptionKey = "option.lang")
    String lang;

    @Option(names = "--config", paramLabel = "FILE", descriptionKey = "option.config")
    Path config;

    @Option(names = "--ditto", descriptionKey = "option.ditto")
    boolean ditto;

    Messages messages() {
        try {
            return new Messages(Messages.resolve(lang, Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            throw new ParameterException(spec.commandLine(), "--lang = " + lang + ": must be es or en");
        }
    }

    Curve curve(Messages messages) {
        return curves(messages).forParent(ditto);
    }

    String curveLabel(Messages messages) {
        return messages.get(ditto ? "curve.ditto" : "curve.main");
    }

    private BreedingCurves curves(Messages messages) {
        if (config == null) {
            return BreedingCurves.DEFAULT;
        }
        String json;
        try {
            json = Files.readString(config);
        } catch (IOException e) {
            throw new InvalidConfigException(List.of(messages.get("error.read", config, e.getMessage())));
        }
        return switch (ConfigParser.parse(json)) {
            case ConfigResult.Valid valid -> valid.config().effective();
            case ConfigResult.Invalid invalid -> throw new InvalidConfigException(invalid.errors());
        };
    }

    static final class InvalidConfigException extends RuntimeException {
        private final List<String> errors;

        InvalidConfigException(List<String> errors) {
            super(String.join("; ", errors));
            this.errors = List.copyOf(errors);
        }

        List<String> errors() {
            return errors;
        }
    }
}
