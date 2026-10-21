package io.github.bernhardtwo.breedinglifecycle.cli;

import java.util.Locale;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "cli",
        resourceBundle = "messages",
        mixinStandardHelpOptions = true,
        subcommands = {CurveCommand.class, SimulateCommand.class, CompareCommand.class})
public final class Main {
    static final int USAGE_ERROR = 2;

    public static void main(String[] args) {
        String lang = Messages.langIn(args);
        if (lang != null && (lang.equals("es") || lang.equals("en"))) {
            Locale.setDefault(Messages.resolve(lang, Locale.getDefault()));
        }
        System.exit(commandLine().execute(args));
    }

    static CommandLine commandLine() {
        var cli = new CommandLine(new Main());
        var help = new Messages(Messages.resolve(null, Locale.getDefault()));
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
