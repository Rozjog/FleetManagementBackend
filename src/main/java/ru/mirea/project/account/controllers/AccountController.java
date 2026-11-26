package ru.mirea.project.account.controllers;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ru.mirea.project.account.dto.*;
import ru.mirea.project.account.services.AccountService;
import ru.mirea.project.auth.dto.AccountResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Page<AccountResponse> getAccounts(
            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return accountService.getAccounts(
                search,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(
            @PathVariable UUID id
    ) {
        return accountService.getAccount(id);
    }

    @PostMapping
    public ResponseEntity<TemporaryPasswordResponse> createAccount(
            @Valid @RequestBody AccountCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(accountService.createAccount(request));
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        return accountService.updateAccount(
                id,
                UUID.fromString(jwt.getSubject()),
                request
        );
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<TemporaryPasswordResponse> resetPassword(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(accountService.resetPassword(
                        id,
                        UUID.fromString(jwt.getSubject())
                ));
    }
}
