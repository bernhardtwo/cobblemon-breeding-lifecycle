package io.github.bernhardtwo.breedinglifecycle.screenshots;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Drives the dev client through the in-game screenshot scenes, one action per tick, on the client thread. */
public final class SceneRunner {
    private static final Logger LOG = LoggerFactory.getLogger("breedinglifecycle-screenshots");
    private static final String PLAYER = "Dev";
    private static final long TIMEOUT_MILLIS = 30_000;
    private static final int SETTLE_TICKS = 40;
    private static final int CAPTURE_DELAY_TICKS = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final Minecraft minecraft;
    private final Path serverConfig;
    private final Path outputDir;
    private final Deque<Action> queue = new ArrayDeque<>();
    private Action current;
    private boolean started;
    private boolean finished;
    private String lastReply = "";
    private UUID male;
    private UUID female;
    private int attempts;

    public SceneRunner(Minecraft minecraft, Path serverConfig, Path outputDir) {
        this.minecraft = minecraft;
        this.serverConfig = serverConfig;
        this.outputDir = outputDir;
    }

    /** Called at the end of every client tick. */
    public void tick() {
        if (finished || minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }
        try {
            if (!started) {
                started = true;
                plan();
            }
            while (true) {
                if (current == null) {
                    current = queue.poll();
                    if (current == null) {
                        return;
                    }
                    current.start();
                }
                if (!current.done(System.currentTimeMillis())) {
                    return;
                }
                current = null;
            }
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    /** Called for every system chat message the client receives. */
    public void onMessage(String message) {
        if (current instanceof Command command) {
            command.offer(message, System.currentTimeMillis());
        }
    }

    private void plan() {
        queue.add(ticks(SETTLE_TICKS));
        queue.add(run(this::setUpClient));
        queue.add(until(() -> minecraft.getOverlay() == null, "the resource reload"));
        queue.addAll(statusScene());
        queue.addAll(infoScene());
        queue.addAll(lossScene());
        queue.addAll(refusedScene());
        queue.add(run(() -> writeConfig(Scenes.DEFAULT_CONFIG)));
        queue.add(new Command(Scenes.reload()));
        queue.add(run(this::done));
    }

    private void setUpClient() {
        if (!"en_us".equals(minecraft.options.languageCode)) {
            minecraft.getLanguageManager().setSelected("en_us");
            minecraft.options.languageCode = "en_us";
            minecraft.reloadResourcePacks();
        }
        minecraft.options.guiScale().set(2);
        minecraft.options.joinedFirstServer = true;
        minecraft.getTutorial().setStep(TutorialSteps.NONE);
        minecraft.resizeDisplay();
        minecraft.options.save();
    }

    private List<Action> statusScene() {
        List<Action> actions = new ArrayList<>(freshPair());
        actions.add(run(this::clearChat));
        actions.add(deferred(() -> commands(Scenes.status(slotOf(male), slotOf(female)))));
        actions.add(deferred(() -> {
            if (lastReply.contains("breedings:")) {
                return capture("ingame-daycare-status");
            }
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                throw new IllegalStateException("both parents lost " + MAX_ATTEMPTS + " times");
            }
            List<Action> retry = new ArrayList<>();
            retry.add(new Command(new Step("daycare withdraw", "returned to your party or PC")));
            retry.addAll(statusScene());
            return retry;
        }));
        return actions;
    }

    private List<Action> infoScene() {
        List<Step> steps = Scenes.info(0);
        List<Action> actions = new ArrayList<>();
        actions.add(run(this::clearChat));
        actions.add(new Command(steps.get(0)));
        actions.add(deferred(() -> commands(List.of(Scenes.info(survivorSlot()).get(1)))));
        actions.addAll(capture("ingame-info"));
        return actions;
    }

    private List<Action> lossScene() {
        List<Action> actions = new ArrayList<>();
        actions.add(run(() -> writeConfig(Scenes.LOSS_CONFIG)));
        actions.add(new Command(Scenes.reload()));
        actions.addAll(freshPair());
        actions.add(run(this::clearChat));
        actions.add(deferred(() -> commands(Scenes.loss(slotOf(male), slotOf(female)))));
        actions.add(run(() -> {
            if (!lastReply.contains("Parent 1 is gone: breeding 1, loss chance 1.0000")) {
                throw new IllegalStateException("parent 1 was not lost with p1 = 1.0: " + lastReply);
            }
        }));
        actions.addAll(capture("ingame-loss"));
        return actions;
    }

    private List<Action> refusedScene() {
        List<Action> actions = new ArrayList<>(freshPair());
        actions.add(run(() -> new SendOutPokemonPacket(slotOf(male) - 1).sendToServer()));
        actions.add(until(this::maleIsOut, "the Charmander sent out of its Poké Ball"));
        actions.add(ticks(SETTLE_TICKS));
        actions.add(run(this::clearChat));
        actions.add(deferred(() -> commands(Scenes.refused(slotOf(male)))));
        actions.addAll(capture("ingame-refused"));
        return actions;
    }

    private List<Action> freshPair() {
        List<Action> actions = new ArrayList<>(commands(Scenes.fresh(PLAYER)));
        actions.add(until(this::trackPair, "a male and a female Charmander in the party"));
        return actions;
    }

    private boolean trackPair() {
        male = null;
        female = null;
        for (Pokemon pokemon : partySlots()) {
            if (pokemon == null) {
                continue;
            }
            if (pokemon.getGender() == Gender.MALE && male == null) {
                male = pokemon.getUuid();
            } else if (pokemon.getGender() == Gender.FEMALE && female == null) {
                female = pokemon.getUuid();
            }
        }
        return male != null
                && female != null
                && partySlots().stream().filter(p -> p != null).count() == 2;
    }

    private int survivorSlot() {
        List<UUID> slots = partyUuids();
        return slots.contains(male) ? PartyPick.slotOf(slots, male) : PartyPick.slotOf(slots, female);
    }

    private boolean maleIsOut() {
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof PokemonEntity pokemon
                    && male.equals(pokemon.getPokemon().getUuid())) {
                return true;
            }
        }
        return false;
    }

    private int slotOf(UUID uuid) {
        return PartyPick.slotOf(partyUuids(), uuid);
    }

    private List<Pokemon> partySlots() {
        return CobblemonClient.INSTANCE.getStorage().getParty().getSlots();
    }

    private List<UUID> partyUuids() {
        List<UUID> uuids = new ArrayList<>();
        for (Pokemon pokemon : partySlots()) {
            uuids.add(pokemon == null ? null : pokemon.getUuid());
        }
        return uuids;
    }

    private void clearChat() {
        minecraft.gui.getChat().clearMessages(false);
    }

    private List<Action> capture(String name) {
        String file = name + ".png";
        Path taken = minecraft.gameDirectory.toPath().resolve("screenshots").resolve(file);
        return List.of(
                run(() -> {
                    deleteIfExists(taken);
                    minecraft.getToasts().clear();
                    minecraft.setScreen(new ChatScreen(""));
                }),
                ticks(CAPTURE_DELAY_TICKS),
                run(() ->
                        Screenshot.grab(minecraft.gameDirectory, file, minecraft.getMainRenderTarget(), message -> {})),
                until(() -> Files.isRegularFile(taken), "the screenshot " + file),
                ticks(CAPTURE_DELAY_TICKS),
                run(() -> {
                    copy(taken, outputDir.resolve(file));
                    minecraft.setScreen(null);
                    LOG.info("[screenshots] saved {}", outputDir.resolve(file));
                }));
    }

    private void done() {
        finished = true;
        LOG.info("[screenshots] DONE");
        minecraft.stop();
    }

    private void fail(String reason) {
        finished = true;
        LOG.error("[screenshots] FAILED: {}", reason);
        try {
            writeConfig(Scenes.DEFAULT_CONFIG);
        } catch (RuntimeException e) {
            LOG.error("[screenshots] could not restore the server config", e);
        }
        System.exit(1);
    }

    private void writeConfig(String config) {
        try {
            Files.writeString(serverConfig, config);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copy(Path from, Path to) {
        try {
            Files.createDirectories(to.getParent());
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Action> commands(List<Step> steps) {
        List<Action> actions = new ArrayList<>();
        for (Step step : steps) {
            actions.add(new Command(step));
        }
        return actions;
    }

    private Action deferred(Supplier<List<Action>> next) {
        return run(() -> {
            List<Action> actions = next.get();
            for (int i = actions.size() - 1; i >= 0; i--) {
                queue.addFirst(actions.get(i));
            }
        });
    }

    private static Action run(Runnable body) {
        return new Action() {
            @Override
            public void start() {
                body.run();
            }

            @Override
            public boolean done(long now) {
                return true;
            }
        };
    }

    private static Action ticks(int count) {
        return new Action() {
            private int left;

            @Override
            public void start() {
                left = count;
            }

            @Override
            public boolean done(long now) {
                return left-- <= 0;
            }
        };
    }

    private static Action until(BooleanSupplier condition, String what) {
        return new Action() {
            private long since;

            @Override
            public void start() {
                since = System.currentTimeMillis();
            }

            @Override
            public boolean done(long now) {
                if (condition.getAsBoolean()) {
                    return true;
                }
                if (now - since >= TIMEOUT_MILLIS) {
                    throw new IllegalStateException("timed out waiting for " + what);
                }
                return false;
            }
        };
    }

    private interface Action {
        void start();

        boolean done(long now);
    }

    private final class Command implements Action {
        private final Step step;
        private ReplyWait wait;
        private boolean replied;

        Command(Step step) {
            this.step = step;
        }

        @Override
        public void start() {
            wait = new ReplyWait(step, System.currentTimeMillis(), TIMEOUT_MILLIS);
            minecraft.getConnection().sendCommand(step.command());
        }

        void offer(String message, long now) {
            if (!replied && wait.accept(message, now)) {
                replied = true;
                lastReply = message;
            }
        }

        @Override
        public boolean done(long now) {
            if (replied) {
                return true;
            }
            wait.failure(now).ifPresent(reason -> {
                throw new IllegalStateException(reason);
            });
            return false;
        }
    }
}
