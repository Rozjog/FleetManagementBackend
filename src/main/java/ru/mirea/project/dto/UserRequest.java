package ru.mirea.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.mirea.project.enums.UserStatus;

public record UserRequest(

        @NotBlank(message = "Имя обязательно.")
        @Size(max = 100, message = "Имя не должно превышать 100 символов.")
        String firstName,

        @NotBlank(message = "Фамилия обязательна.")
        @Size(max = 100, message = "Фамилия не должна превышать 100 символов.")
        String lastName,

        @NotBlank(message = "Телефон обязателен.")
        @Pattern(
                regexp = "^\\+7\\d{10}$",
                message = "Телефон должен быть в формате +79991234567."
        )
        String phone,

        @Email(message = "Некорректный email.")
        @Size(max = 255, message = "Email слишком длинный.")
        String email,

        @Pattern(
                regexp = "^(\\d{10})?$",
                message = "Водительское удостоверение должно содержать 10 цифр."
        )
        String driverLicense,

        UserStatus status
) {
}
