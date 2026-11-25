package ru.mirea.project.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.mirea.project.user.enums.UserStatus;

import java.util.List;

public record UserBulkStatusRequest(

        @NotEmpty(message = "Укажите хотя бы одного пользователя.")
        @Size(max = 1000, message = "Максимум 1000 пользователей.")
        List<@NotNull @Positive Long> userIds,

        @NotNull(message = "Укажите новый статус.")
        UserStatus status
) {
}
