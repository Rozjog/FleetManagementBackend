package ru.mirea.project.car.dto;

import java.util.List;

public record CarBulkResponse(
        List<Long> successfulIds,
        List<Failure> errors
) {
    public record Failure(Long id, String message) {}
}