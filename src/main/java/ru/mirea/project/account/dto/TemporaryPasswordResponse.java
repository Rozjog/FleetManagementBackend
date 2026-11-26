package ru.mirea.project.account.dto;

import ru.mirea.project.auth.dto.AccountResponse;

public record TemporaryPasswordResponse(
        AccountResponse account,
        String temporaryPassword
) {}