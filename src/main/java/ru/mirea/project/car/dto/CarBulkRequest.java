package ru.mirea.project.car.dto;

import jakarta.validation.constraints.*;
import ru.mirea.project.car.enums.CarBulkOperation;
import ru.mirea.project.car.enums.CarStatus;

import java.util.List;

public record CarBulkRequest(
        @NotEmpty
        @Size(max = 100)
        List<@NotNull @Positive Long> ids,

        @NotNull
        CarBulkOperation operation,

        CarStatus status
) {}