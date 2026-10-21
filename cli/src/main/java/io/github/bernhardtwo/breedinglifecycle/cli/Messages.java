package io.github.bernhardtwo.breedinglifecycle.cli;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/** Localized CLI text and number formatting for Spanish or English. */
final class Messages {
    static final Locale SPANISH = Locale.forLanguageTag("es");

    private final Locale locale;
    private final ResourceBundle bundle;

    Messages(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle(
                "messages",
                locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    }

    static Locale resolve(String langOption, Locale system) {
        if (langOption == null) {
            return system != null && "es".equals(system.getLanguage()) ? SPANISH : Locale.ENGLISH;
        }
        return switch (langOption) {
            case "es" -> SPANISH;
            case "en" -> Locale.ENGLISH;
            default -> throw new IllegalArgumentException(langOption);
        };
    }

    static String langIn(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--lang") && i + 1 < args.length) {
                return args[i + 1];
            }
            if (args[i].startsWith("--lang=")) {
                return args[i].substring("--lang=".length());
            }
        }
        return null;
    }

    Locale locale() {
        return locale;
    }

    String get(String key, Object... args) {
        return new MessageFormat(bundle.getString(key), locale).format(args);
    }

    String decimal(double value) {
        var format = NumberFormat.getNumberInstance(locale);
        format.setMinimumFractionDigits(4);
        format.setMaximumFractionDigits(4);
        format.setGroupingUsed(false);
        return format.format(value);
    }

    String percent(double fraction) {
        var format = NumberFormat.getNumberInstance(locale);
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(fraction * 100) + "%";
    }
}
