package io.github.bernhardtwo.breedinglifecycle.screenshots;

/** A chat command, without its slash, and the text its reply must contain, or an alternative text, which may be null. */
public record Step(String command, String expected, String alternative) {
    public Step(String command, String expected) {
        this(command, expected, null);
    }

    /** Whether {@code message} is a reply this step accepts. */
    public boolean matches(String message) {
        return message.contains(expected) || (alternative != null && message.contains(alternative));
    }
}
