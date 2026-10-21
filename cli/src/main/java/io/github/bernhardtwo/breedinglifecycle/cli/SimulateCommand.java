package io.github.bernhardtwo.breedinglifecycle.cli;

import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import io.github.bernhardtwo.breedinglifecycle.core.SeededRandom;
import io.github.bernhardtwo.breedinglifecycle.core.Simulator;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "simulate", mixinStandardHelpOptions = true)
final class SimulateCommand implements Callable<Integer> {
    private static final int BAR_WIDTH = 40;

    @Mixin
    CommonOptions options;

    @Spec
    CommandSpec spec;

    @Option(names = "--parents", defaultValue = "10000", paramLabel = "N", descriptionKey = "option.parents")
    int parents;

    @Option(names = "--seed", paramLabel = "SEED", descriptionKey = "option.seed")
    Long seed;

    @Override
    public Integer call() {
        var messages = options.messages();
        if (parents < 1) {
            throw new ParameterException(spec.commandLine(), messages.get("error.parents", parents));
        }
        Curve curve = options.curve(messages);
        long usedSeed = seed != null ? seed : new SecureRandom().nextLong();
        var result = Simulator.run(curve, parents, new SeededRandom(usedSeed));
        var out = spec.commandLine().getOut();
        out.println(Format.curveHeader(messages, options.curveLabel(messages), curve));
        out.println(messages.get("simulate.header", Integer.toString(parents), Long.toString(usedSeed)));
        out.println();
        out.println(messages.get(
                "simulate.mean", messages.decimal(result.mean()), messages.decimal(curve.expectedOffspring())));
        out.println(messages.get(
                "simulate.percentiles",
                Integer.toString(result.p50()),
                Integer.toString(result.p90()),
                Integer.toString(result.p99()),
                Integer.toString(result.max())));
        out.println();
        out.println(messages.get("simulate.distribution"));
        int largest = result.histogram().values().stream().max(Integer::compare).orElse(1);
        result.histogram()
                .forEach((offspring, count) -> out.printf(
                        "%4d  %7d  %7s  %s%n",
                        offspring, count, messages.percent((double) count / parents), "█".repeat(Math.max(1, (int)
                                        Math.round((double) count / largest * BAR_WIDTH)))));
        out.flush();
        return 0;
    }
}
