package ru.mirea.project.account.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mirea.project.account.dto.*;
import ru.mirea.project.account.enums.AccountRole;
import ru.mirea.project.account.models.UserAccount;
import ru.mirea.project.account.repositories.UserAccountRepository;
import ru.mirea.project.auth.dto.AccountResponse;
import ru.mirea.project.auth.services.RefreshSessionService;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshSessionService refreshSessionService;

    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            RefreshSessionService refreshSessionService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.refreshSessionService = refreshSessionService;
    }

    public Page<AccountResponse> getAccounts(
            String search,
            int page,
            int size
    ) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Некорректные параметры пагинации."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("lastName")
                        .ascending()
                        .and(Sort.by("id"))
        );

        if (search == null || search.isBlank()) {
            return repository.findAll(pageable)
                    .map(AccountResponse::from);
        }

        String query = search.trim();

        return repository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query,
                        query,
                        query,
                        pageable
                )
                .map(AccountResponse::from);
    }

    public AccountResponse getAccount(UUID id) {
        return AccountResponse.from(findAccount(id));
    }

    @Transactional
    public TemporaryPasswordResponse createAccount(
            AccountCreateRequest request
    ) {

        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Учётная запись с таким email уже существует."
            );
        }

        String temporaryPassword = generateTemporaryPassword();

        UserAccount account = new UserAccount();

        account.setFirstName(request.firstName().trim());
        account.setLastName(request.lastName().trim());
        account.setEmail(email);
        account.setRole(request.role());

        account.setPasswordHash(
                passwordEncoder.encode(temporaryPassword)
        );

        account.setActive(true);
        account.setMustChangePassword(true);

        UserAccount saved = repository.save(account);

        return new TemporaryPasswordResponse(
                AccountResponse.from(saved),
                temporaryPassword
        );
    }

    @Transactional
    public AccountResponse updateAccount(
            UUID id,
            UUID currentAdminId,
            AccountUpdateRequest request
    ) {

        UserAccount account = findAccount(id);

        String email = normalizeEmail(request.email());

        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException(
                    "Учётная запись с таким email уже существует."
            );
        }

        if (id.equals(currentAdminId)) {

            if (!request.active()) {
                throw new IllegalArgumentException(
                        "Нельзя отключить собственную учётную запись."
                );
            }

            if (request.role() != AccountRole.ADMIN) {
                throw new IllegalArgumentException(
                        "Нельзя изменить собственную роль администратора."
                );
            }
        }

        account.setFirstName(request.firstName().trim());
        account.setLastName(request.lastName().trim());
        account.setEmail(email);
        account.setRole(request.role());
        account.setActive(request.active());

        UserAccount saved = repository.save(account);

        if (!saved.isActive()) {
            refreshSessionService.revokeAllSessions(id);
        }

        return AccountResponse.from(saved);
    }

    @Transactional
    public TemporaryPasswordResponse resetPassword(
            UUID id,
            UUID currentAdminId
    ) {

        UserAccount account = findAccount(id);

        if (id.equals(currentAdminId)) {
            throw new IllegalArgumentException(
                    "Для собственного аккаунта используйте смену пароля."
            );
        }

        String temporaryPassword = generateTemporaryPassword();

        account.setPasswordHash(
                passwordEncoder.encode(temporaryPassword)
        );

        account.setMustChangePassword(true);

        UserAccount saved = repository.save(account);

        refreshSessionService.revokeAllSessions(id);

        return new TemporaryPasswordResponse(
                AccountResponse.from(saved),
                temporaryPassword
        );
    }

    private UserAccount findAccount(UUID id) {

        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Учётная запись не найдена."
                ));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateTemporaryPassword() {

        byte[] bytes = new byte[18];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
