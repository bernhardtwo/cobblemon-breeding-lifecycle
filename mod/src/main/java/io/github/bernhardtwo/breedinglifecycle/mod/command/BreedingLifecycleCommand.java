package io.github.bernhardtwo.breedinglifecycle.mod.command;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.bernhardtwo.breedinglifecycle.core.Curve;
import io.github.bernhardtwo.breedinglifecycle.core.config.LifecycleConfig;
import io.github.bernhardtwo.breedinglifecycle.mod.BreedingCounter;
import io.github.bernhardtwo.breedinglifecycle.mod.LifecycleService;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /breedinglifecycle reload | info <slot> | curve}. */
public final class BreedingLifecycleCommand {
    private static final String KEY = "breedinglifecycle.";
    private static final double SHOWN_SURVIVAL = 0.0001;
    private static final int MAX_ROWS = 30;
    private static final int PARTY_SIZE = 6;

    private BreedingLifecycleCommand() {}

    /** Registers the command tree; {@code reload} needs operator level 2. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("breedinglifecycle")
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(BreedingLifecycleCommand::reload))
                .then(Commands.literal("info")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, PARTY_SIZE))
                                .executes(BreedingLifecycleCommand::info)))
                .then(Commands.literal("curve").executes(BreedingLifecycleCommand::curve)));
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        List<String> errors = LifecycleService.get().config().reload();
        var source = context.getSource();
        if (errors.isEmpty()) {
            source.sendSuccess(() -> text("reload.ok"), true);
            return 1;
        }
        source.sendFailure(text("reload.failed"));
        errors.forEach(error -> source.sendFailure(text("reload.error", error)));
        return 0;
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int slot = IntegerArgumentType.getInteger(context, "slot");
        Pokemon pokemon = Cobblemon.INSTANCE.getStorage().getParty(player).get(slot - 1);
        if (pokemon == null) {
            context.getSource().sendFailure(text("info.empty_slot", slot));
            return 0;
        }
        int breedings = BreedingCounter.get(pokemon);
        Curve curve = LifecycleService.get()
                .config()
                .current()
                .lifecycle()
                .effective()
                .forParent(LifecycleService.isDitto(pokemon));
        double nextP = curve.lossProbability(breedings == Integer.MAX_VALUE ? breedings : breedings + 1);
        context.getSource()
                .sendSuccess(
                        () -> text(
                                "info.result",
                                slot,
                                pokemon.getSpecies().getTranslatedName(),
                                breedings,
                                decimal(nextP)),
                        false);
        return 1;
    }

    private static int curve(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        LifecycleConfig config = LifecycleService.get().config().current().lifecycle();
        source.sendSuccess(() -> text("curve.header", config.preset()), false);
        table(source, "curve.main", config.effective().main());
        table(source, "curve.ditto", config.effective().ditto());
        return 1;
    }

    private static void table(CommandSourceStack source, String title, Curve curve) {
        Component max = curve.maxBreedings() == null
                ? text("curve.none")
                : Component.literal(curve.maxBreedings().toString());
        source.sendSuccess(() -> text(title, decimal(curve.p1()), decimal(curve.r()), max), false);
        source.sendSuccess(() -> text("curve.columns"), false);
        for (int n = 1; n <= MAX_ROWS; n++) {
            double survival = curve.survival(n);
            if (n > 1 && survival < SHOWN_SURVIVAL) {
                break;
            }
            int row = n;
            source.sendSuccess(
                    () -> text("curve.row", row, decimal(curve.lossProbability(row)), decimal(survival)), false);
            if (curve.maxBreedings() != null && n >= curve.maxBreedings()) {
                break;
            }
        }
        source.sendSuccess(() -> text("curve.expected", decimal(curve.expectedOffspring())), false);
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(KEY + key, args);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
