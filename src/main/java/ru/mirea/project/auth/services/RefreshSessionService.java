package ru.mirea.project.auth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mirea.project.account.models.UserAccount;
import ru.mirea.project.account.repositories.UserAccountRepository;
import ru.mirea.project.auth.dto.AccountResponse;
import ru.mirea.project.auth.dto.LoginResponse;
import ru.mirea.project.auth.models.RefreshSession;
import ru.mirea.project.auth.repositories.RefreshSessionRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshSessionService {

    private final RefreshSessionRepository repository;
    private final UserAccountRepository accountRepository;
    private final JwtService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

    private final long refreshTokenDays;

    public RefreshSessionService(
            RefreshSessionRepository repository,
            UserAccountRepository accountRepository,
            JwtService jwtService,
            @Value("${REFRESH_TOKEN_DAYS:14}")
            long refreshTokenDays
    ) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;

        if (refreshTokenDays <= 0) {
            throw new IllegalArgumentException(
                    "Некорректный срок действия Refresh Token."
            );
        }

        this.refreshTokenDays = refreshTokenDays;
    }

    // Создание новой сессии
    @Transactional
    public String createSession(UUID accountId) {

        String refreshToken = generateToken();

        RefreshSession session = new RefreshSession();

        session.setAccountId(accountId);
        session.setTokenHash(hashToken(refreshToken));
        session.setExpiresAt(
                OffsetDateTime.now().plusDays(refreshTokenDays)
        );

        repository.save(session);

        return refreshToken;
    }

    // Обновление пары токенов
    @Transactional
    public LoginResponse refresh(String refreshToken) {

        RefreshSession session = repository
                .findByTokenHash(hashToken(refreshToken))
                .orElseThrow(this::invalidToken);

        if (!session.getExpiresAt()
                .isAfter(OffsetDateTime.now())) {
            throw invalidToken();
        }

        UserAccount account = accountRepository
                .findById(session.getAccountId())
                .filter(UserAccount::isActive)
                .orElseThrow(this::invalidToken);

        if (account.isMustChangePassword()) {
            throw invalidToken();
        }

        String newRefreshToken = generateToken();

        session.setTokenHash(
                hashToken(newRefreshToken)
        );

        repository.save(session);

        return new LoginResponse(
                jwtService.generateAccessToken(account),
                newRefreshToken,
                "Bearer",
                jwtService.getExpiresIn(),
                AccountResponse.from(account)
        );
    }

    // Завершение одной сессии
    @Transactional
    public void revokeSession(String refreshToken) {

        repository.deleteByTokenHash(
                hashToken(refreshToken)
        );
    }

    // Завершение всех сессий сотрудника
    @Transactional
    public void revokeAllSessions(UUID accountId) {

        repository.deleteByAccountId(accountId);
    }

    // Генерация случайного токена
    private String generateToken() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Хеширование токена
    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Ошибка хеширования токена.", e
            );
        }
    }

    private BadCredentialsException invalidToken() {

        return new BadCredentialsException(
                "Недействительный Refresh Token."
        );
    }
}
