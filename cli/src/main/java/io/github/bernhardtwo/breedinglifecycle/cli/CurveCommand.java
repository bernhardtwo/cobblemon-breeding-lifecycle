package io.github.bernhardtwo.breedinglifecycle.cli;

import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "curve", mixinStandardHelpOptions = true)
final class CurveCommand implements Callable<Integer> {
    private static final double SHOWN_SURVIVAL = 0.0001;
    private static final int MAX_ROWS = 30;

    @Mixin
    CommonOptions options;

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() {
        var messages = options.messages();
        Curve curve = options.curve(messages);
        var out = spec.commandLine().getOut();
        out.println(Format.curveHeader(messages, options.curveLabel(messages), curve));
        out.println();
        out.printf("%4s  %8s  %8s%n", "n", "p(n)", "S(n)");
        for (int n = 1; n <= MAX_ROWS; n++) {
            double survival = curve.survival(n);
            if (n > 1 && survival < SHOWN_SURVIVAL) {
                break;
            }
            out.printf("%4d  %8s  %8s%n", n, messages.decimal(curve.lossProbability(n)), messages.decimal(survival));
            if (curve.maxBreedings() != null && n >= curve.maxBreedings()) {
                break;
            }
        }
        out.println();
        out.println(messages.get("curve.expectedNote"));
        out.println("E = " + messages.decimal(curve.expectedOffspring()));
        out.flush();
        return 0;
    }
}
