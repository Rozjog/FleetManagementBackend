package ru.mirea.project.auth.dto;

import ru.mirea.project.account.enums.AccountRole;
import ru.mirea.project.account.models.UserAccount;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        AccountRole role,
        boolean active,
        boolean mustChangePassword
) {

    public static AccountResponse from(UserAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getEmail(),
                account.getRole(),
                account.isActive(),
                account.isMustChangePassword()
        );
    }
}
