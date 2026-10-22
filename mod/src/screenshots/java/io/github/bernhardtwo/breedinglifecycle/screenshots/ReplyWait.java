package io.github.bernhardtwo.breedinglifecycle.screenshots;

import java.util.Optional;

/** Waits for the reply to one sent {@link Step}; messages from before the command never count. */
public final class ReplyWait {
    private final Step step;
    private final long sentAtMillis;
    private final long timeoutMillis;

    public ReplyWait(Step step, long sentAtMillis, long timeoutMillis) {
        this.step = step;
        this.sentAtMillis = sentAtMillis;
        this.timeoutMillis = timeoutMillis;
    }

    public Step step() {
        return step;
    }

    /** Whether this message, received at that time, is the expected reply. */
    public boolean accept(String message, long atMillis) {
        return atMillis > sentAtMillis && step.matches(message);
    }

    /** The failure to report once the timeout has passed without a reply. */
    public Optional<String> failure(long nowMillis) {
        if (nowMillis - sentAtMillis < timeoutMillis) {
            return Optional.empty();
        }
        return Optional.of("timed out waiting for '" + step.expected() + "' after /" + step.command());
    }
}
