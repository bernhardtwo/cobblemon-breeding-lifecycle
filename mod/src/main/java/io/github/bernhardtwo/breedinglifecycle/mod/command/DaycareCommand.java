package io.github.bernhardtwo.breedinglifecycle.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.BreedOutcome;
import io.github.bernhardtwo.breedinglifecycle.mod.daycare.Daycare;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /daycare deposit <slot> | breed | status | withdraw}; always registered; deposit and breed need it enabled. */
public final class DaycareCommand {
    private static final int PARTY_SIZE = 6;

    private DaycareCommand() {}

    /** Registers the command tree, usable by every player. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("daycare")
                .then(Commands.literal("deposit")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, PARTY_SIZE))
                                .executes(context -> reply(
                                        context,
                                        player -> Daycare.deposit(
                                                player, IntegerArgumentType.getInteger(context, "slot"))))))
                .then(Commands.literal("breed").executes(DaycareCommand::breed))
                .then(Commands.literal("status").executes(context -> reply(context, Daycare::status)))
                .then(Commands.literal("withdraw").executes(context -> reply(context, Daycare::withdraw))));
    }

    private static int breed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BreedOutcome outcome = Daycare.breed(context.getSource().getPlayerOrException());
        Component message = Daycare.describe(outcome);
        if (outcome instanceof BreedOutcome.Bred) {
            context.getSource().sendSuccess(() -> message, false);
            return 1;
        }
        context.getSource().sendFailure(message);
        return 0;
    }

    private static int reply(CommandContext<CommandSourceStack> context, Function<ServerPlayer, Component> action)
            throws CommandSyntaxException {
        Component message = action.apply(context.getSource().getPlayerOrException());
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }
}
