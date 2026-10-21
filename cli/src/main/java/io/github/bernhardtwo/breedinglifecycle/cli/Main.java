package io.github.bernhardtwo.breedinglifecycle.cli;

import java.util.Locale;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** Entry point of the breeding lifecycle simulator. */
@Command(
        name = "cli",
        mixinStandardHelpOptions = true,
        subcommands = {CurveCommand.class, SimulateCommand.class, CompareCommand.class})
public final class Main {
    static final int USAGE_ERROR = 2;

    private Main() {}

    public static void main(String[] args) {
        String lang = Messages.langIn(args);
        boolean known = "es".equals(lang) || "en".equals(lang);
        System.exit(commandLine(known ? lang : null).execute(args));
    }

    static CommandLine commandLine() {
        return commandLine(null);
    }

    static CommandLine commandLine(String helpLang) {
        var cli = new CommandLine(new Main());
        var help = new Messages(Messages.resolve(helpLang, Locale.getDefault()));
        cli.setResourceBundle(help.bundle());
        cli.getCommandSpec().usageMessage().description(help.get("command.root"));
        cli.getSubcommands()
                .forEach((name, sub) -> sub.getCommandSpec().usageMessage().description(help.get("command." + name)));
        cli.setExecutionExceptionHandler((e, commandLine, parseResult) -> {
            if (e instanceof CommonOptions.InvalidConfigException invalid) {
                var messages = new Messages(Messages.resolve(
                        Messages.langIn(parseResult.originalArgs().toArray(String[]::new)), Locale.getDefault()));
                commandLine.getErr().println(messages.get("error.config"));
                invalid.errors().forEach(error -> commandLine.getErr().println("  " + error));
                commandLine.getErr().flush();
                return USAGE_ERROR;
            }
            throw e;
        });
        return cli;
    }
}
