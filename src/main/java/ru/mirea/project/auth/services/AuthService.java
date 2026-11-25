package ru.mirea.project.auth.services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mirea.project.account.models.UserAccount;
import ru.mirea.project.account.repositories.UserAccountRepository;
import ru.mirea.project.auth.dto.*;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshSessionService refreshSessionService;
    private final String dummyHash;

    public AuthService(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshSessionService refreshSessionService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshSessionService = refreshSessionService;
        this.dummyHash = passwordEncoder.encode(
                UUID.randomUUID().toString()
        );
    }


    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        UserAccount account = repository.findByEmail(email)
                .orElse(null);

        if (request.password()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException(
                    "Неверный email или пароль."
            );
        }

        String hash = account == null
                ? dummyHash
                : account.getPasswordHash();

        boolean correctPassword = passwordEncoder.matches(
                request.password(),
                hash
        );

        if (account == null ||
                !correctPassword ||
                !account.isActive()) {

            throw new BadCredentialsException(
                    "Неверный email или пароль."
            );
        }

        String refreshToken = null;

        if (!account.isMustChangePassword()) {
            refreshToken = refreshSessionService.createSession(
                    account.getId()
            );
        }

        return new LoginResponse(
                jwtService.generateAccessToken(account),
                refreshToken,
                "Bearer",
                jwtService.getExpiresIn(),
                AccountResponse.from(account)
        );
    }


    public AccountResponse getCurrentAccount(UUID id) {

        return AccountResponse.from(getActiveAccount(id));
    }

    @Transactional
    public void changePassword(
            UUID id,
            ChangePasswordRequest request
    ) {

        UserAccount account = getActiveAccount(id);

        if (request.oldPassword()
                .getBytes(StandardCharsets.UTF_8).length > 72 ||
                !passwordEncoder.matches(
                        request.oldPassword(),
                        account.getPasswordHash()
                )) {
            throw new BadCredentialsException(
                    "Неверный текущий пароль."
            );
        }

        String password = request.newPassword();

        if (password.length() < 4 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Некорректная длина нового пароля."
            );
        }

        if (passwordEncoder.matches(
                password,
                account.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Новый пароль должен отличаться от старого."
            );
        }

        account.setPasswordHash(
                passwordEncoder.encode(password)
        );

        account.setMustChangePassword(false);

        repository.save(account);

        refreshSessionService.revokeAllSessions(account.getId());
    }

    private UserAccount getActiveAccount(UUID id) {

        return repository.findById(id)
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new BadCredentialsException(
                        "Учётная запись недоступна."
                ));
    }
}
