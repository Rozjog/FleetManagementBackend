package ru.mirea.project.auth.exceptions;

public class TooManyLoginAttemptsException
        extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(long retryAfterSeconds) {
        super("Слишком много попыток входа. Попробуйте позже.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}