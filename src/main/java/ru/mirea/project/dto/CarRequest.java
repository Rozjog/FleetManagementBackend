
package ru.mirea.project.dto;

import jakarta.validation.constraints.*;
import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.enums.CarTransmission;

public record CarRequest(

        @NotBlank(message = "Марка автомобиля обязательна.")
        @Size(max = 30, message = "Марка не должна быть больше 30 символов.")
        String brand,

        @NotBlank(message = "Модель автомобиля обязательна.")
        @Size(max = 30, message = "Модель не должна быть больше 30 символов.")
        String model,

        @NotNull(message = "Тип коробки передач обязателен.")
        CarTransmission transmission,

        @NotNull(message = "Год выпуска обязателен.")
        @Min(value = 1900, message = "Год выпуска не может быть меньше 1900.")
        Integer year,

        @NotBlank(message = "Госномер обязателен.")
        @Size(max = 30, message = "Госномер не должен быть больше 30 символов.")
        String licensePlate,

        @NotNull(message = "Тип кузова обязателен.")
        CarBodyType bodyType,

        @NotNull(message = "Статус автомобиля обязателен.")
        CarStatus status,

        @NotNull(message = "Пробег обязателен.")
        @Min(value = 0, message = "Пробег не может быть отрицательным.")
        Integer mileage
) {}
