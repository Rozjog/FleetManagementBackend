package ru.mirea.project.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(

        @NotBlank
        @Size(min = 43, max = 43)
        String refreshToken

) {}