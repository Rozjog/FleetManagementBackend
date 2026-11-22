
package ru.mirea.project.dto;

import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.enums.CarTransmission;

import java.time.LocalDateTime;

public record CarResponse(
        Long id,
        String brand,
        String model,
        CarTransmission transmission,
        int year,
        String licensePlate,
        CarBodyType bodyType,
        CarStatus status,
        int mileage,
        LocalDateTime deletedAt
) {}
