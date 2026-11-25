package ru.mirea.project.auth.dto;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(

        @NotBlank
        @Size(max = 72)
        String oldPassword,

        @NotBlank
        @Size(min = 5, max = 72)
        String newPassword

) {}