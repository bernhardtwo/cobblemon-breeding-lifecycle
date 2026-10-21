package io.github.bernhardtwo.breedinglifecycle.cli;

import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "compare", mixinStandardHelpOptions = true)
final class CompareCommand implements Callable<Integer> {
    private static final double CURRENT_PER_PARENT = 1.0;

    @Mixin
    CommonOptions options;

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() {
        var messages = options.messages();
        Curve curve = options.curve(messages);
        double expected = curve.expectedOffspring();
        var out = spec.commandLine().getOut();
        out.println(Format.curveHeader(messages, options.curveLabel(messages), curve));
        out.println();
        String row = "%-24s  %16s  %16s%n";
        out.printf(row, "", messages.get("compare.perParent"), messages.get("compare.perPair"));
        out.printf(
                row,
                messages.get("compare.current"),
                messages.decimal(CURRENT_PER_PARENT),
                messages.decimal(2 * CURRENT_PER_PARENT));
        out.printf(row, messages.get("compare.lifecycle"), messages.decimal(expected), messages.decimal(2 * expected));
        out.println();
        double difference = expected - CURRENT_PER_PARENT;
        String sign = difference >= 0 ? "+" : "";
        out.println(messages.get(
                "compare.difference",
                sign + messages.decimal(difference),
                sign + messages.percent(difference / CURRENT_PER_PARENT)));
        out.flush();
        return 0;
    }
}
