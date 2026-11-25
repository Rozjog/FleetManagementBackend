package ru.mirea.project.user.dto;

import ru.mirea.project.user.enums.UserStatus;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String driverLicense,
        UserStatus status,
        boolean archived
) {}
