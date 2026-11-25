package ru.mirea.project.auth.controllers;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ru.mirea.project.auth.dto.*;
import ru.mirea.project.auth.services.AuthService;
import ru.mirea.project.auth.services.RefreshSessionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshSessionService refreshSessionService;

    public AuthController(
            AuthService authService,
            RefreshSessionService refreshSessionService
    ) {
        this.authService = authService;
        this.refreshSessionService = refreshSessionService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AccountResponse me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return authService.getCurrentAccount(
                UUID.fromString(jwt.getSubject())
        );
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(
                UUID.fromString(jwt.getSubject()),
                request
        );
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return refreshSessionService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        refreshSessionService.revokeSession(
                request.refreshToken()
        );
    }
}
