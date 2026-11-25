package ru.mirea.project.user.dto;

import ru.mirea.project.user.enums.UserStatus;
import java.util.List;

public record UserBulkResponse(
        int updated,
        UserStatus status,
        List<Long> successfulIds
) {}