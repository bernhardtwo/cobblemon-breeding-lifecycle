package io.github.bernhardtwo.breedinglifecycle.mod.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Appends audit entries as JSON lines on its own thread, so the server thread never waits on disk. */
public final class AuditLog implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger("breedinglifecycle");
    private static final String THREAD_NAME = "breedinglifecycle-audit";

    private final Path file;
    private final BooleanSupplier enabled;
    private final Consumer<AuditEntry> afterWrite;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, THREAD_NAME);
        thread.setDaemon(true);
        return thread;
    });

    public AuditLog(Path file, BooleanSupplier enabled) {
        this(file, enabled, entry -> {});
    }

    AuditLog(Path file, BooleanSupplier enabled, Consumer<AuditEntry> afterWrite) {
        this.file = file;
        this.enabled = enabled;
        this.afterWrite = afterWrite;
    }

    /** Queues {@code entry} and returns at once; does nothing while auditing is disabled. */
    public void record(AuditEntry entry) {
        if (!enabled.getAsBoolean()) {
            return;
        }
        String line = entry.toJson() + System.lineSeparator();
        try {
            executor.execute(() -> write(entry, line));
        } catch (RejectedExecutionException e) {
            LOGGER.warn("Audit log closed, dropped entry {}", line.strip());
        }
    }

    /** Waits until every queued entry is written. */
    public void flush() {
        try {
            executor.submit(() -> {}).get();
        } catch (RejectedExecutionException e) {
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOGGER.warn("Audit flush failed", e);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.warn("Audit log did not finish writing within 10 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void write(AuditEntry entry, String line) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            afterWrite.accept(entry);
        } catch (IOException e) {
            LOGGER.warn("Cannot write audit log {}", file, e);
        }
    }
}
