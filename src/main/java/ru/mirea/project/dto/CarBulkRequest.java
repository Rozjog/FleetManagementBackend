package ru.mirea.project.dto;

import jakarta.validation.constraints.*;
import ru.mirea.project.enums.*;

import java.util.List;

public record CarBulkRequest(
        @NotEmpty
        @Size(max = 100)
        List<@NotNull @Positive Long> ids,

        @NotNull
        CarBulkOperation operation,

        CarStatus status
) {}