package ru.mirea.project.account.dto;

import jakarta.validation.constraints.*;
import ru.mirea.project.account.enums.AccountRole;

public record AccountUpdateRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotNull
        AccountRole role,

        @NotNull
        Boolean active

) {}