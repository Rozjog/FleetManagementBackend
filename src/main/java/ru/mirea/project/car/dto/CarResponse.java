
package ru.mirea.project.car.dto;

import ru.mirea.project.car.enums.CarBodyType;
import ru.mirea.project.car.enums.CarStatus;
import ru.mirea.project.car.enums.CarTransmission;

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
