package ru.mirea.project.auth.services;

import org.springframework.stereotype.Service;
import ru.mirea.project.auth.exceptions.TooManyLoginAttemptsException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;

    private static final Duration BLOCK_DURATION =
            Duration.ofSeconds(30);

    private final Map<String, Attempt> attempts =
            new HashMap<>();

    public synchronized void checkBlocked(String email) {

        String key = normalizeEmail(email);
        Attempt attempt = attempts.get(key);

        if (attempt == null || attempt.blockedUntil == null) {
            return;
        }

        Instant now = Instant.now();

        if (!attempt.blockedUntil.isAfter(now)) {
            attempts.remove(key);
            return;
        }

        long milliseconds = Duration.between(
                now, attempt.blockedUntil
        ).toMillis();

        long seconds = Math.max(
                1, (milliseconds + 999) / 1000
        );

        throw new TooManyLoginAttemptsException(seconds);
    }

    public synchronized void registerFailure(String email) {

        checkBlocked(email);

        String key = normalizeEmail(email);

        Attempt attempt = attempts.computeIfAbsent(
                key, value -> new Attempt()
        );

        attempt.count++;

        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.blockedUntil =
                    Instant.now().plus(BLOCK_DURATION);
        }
    }

    public synchronized void registerSuccess(String email) {
        attempts.remove(normalizeEmail(email));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static class Attempt {

        private int count = 0;
        private Instant blockedUntil;
    }
}
