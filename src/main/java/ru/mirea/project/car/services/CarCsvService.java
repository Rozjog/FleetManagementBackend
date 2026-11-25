
package ru.mirea.project.car.services;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.project.car.dto.CarRequest;
import ru.mirea.project.car.enums.CarBodyType;
import ru.mirea.project.car.enums.CarStatus;
import ru.mirea.project.car.enums.CarTransmission;
import ru.mirea.project.car.models.Car;
import ru.mirea.project.car.repositories.CarRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarCsvService {

    private final CarRepository carRepository;
    private final Validator validator;

    private static final List<String> HEADERS = List.of(
            "ID", "Марка", "Модель", "КПП", "Год",
            "Госномер", "Кузов", "Статус", "Пробег"
    );

    public Map<String, Object> validateCars(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV-файл пуст.");
        }

        List<String> errors = new ArrayList<>();
        Set<String> plates = new HashSet<>();

        int totalRows = 0;
        int validRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                )
        )) {
            reader.mark(1);
            if (reader.read() != '\uFEFF') {
                reader.reset();
            }

            try (CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader)) {

                if (!parser.getHeaderNames().equals(HEADERS)) {
                    throw new IllegalArgumentException(
                            "Некорректные заголовки CSV."
                    );
                }

                for (CSVRecord row : parser) {
                    totalRows++;

                    if (totalRows > 1000) {
                        errors.add("Максимум 1000 автомобилей.");
                        break;
                    }

                    try {
                        if (!row.isConsistent()) {
                            throw new IllegalArgumentException(
                                    "Неверное количество столбцов."
                            );
                        }

                        CarRequest data = new CarRequest(
                                row.get("Марка"),
                                row.get("Модель"),
                                CarTransmission.valueOf(row.get("КПП").trim()),
                                Integer.parseInt(row.get("Год").trim()),
                                row.get("Госномер"),
                                CarBodyType.valueOf(row.get("Кузов").trim()),
                                CarStatus.valueOf(row.get("Статус").trim()),
                                Integer.parseInt(row.get("Пробег").trim())
                        );

                        var violations = validator.validate(data);
                        if (!violations.isEmpty()) {
                            throw new IllegalArgumentException(
                                    violations.iterator().next().getMessage()
                            );
                        }

                        Car car = new Car(
                                null, data.brand(), data.model(),
                                data.transmission(), data.year(),
                                data.licensePlate(), data.bodyType(),
                                data.status(), data.mileage()
                        );

                        String plate = car.getLicensePlate()
                                .toUpperCase(Locale.ROOT);

                        if (!plates.add(plate)) {
                            throw new IllegalArgumentException(
                                    "Повторяющийся госномер в CSV."
                            );
                        }

                        if (carRepository.findByLicensePlateIgnoreCase(
                                car.getLicensePlate()).isPresent()) {
                            throw new IllegalArgumentException(
                                    "Госномер уже существует в БД."
                            );
                        }

                        validRows++;

                    } catch (IllegalArgumentException e) {
                        errors.add(
                                "Строка " + (row.getRecordNumber() + 1)
                                        + ": " + e.getMessage()
                        );
                    }
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Не удалось прочитать CSV.", e
            );
        }

        if (totalRows == 0) {
            errors.add("CSV не содержит автомобилей.");
        }

        return Map.of(
                "valid", errors.isEmpty(),
                "totalRows", totalRows,
                "validRows", validRows,
                "errors", errors
        );
    }

    @Transactional
    public int importCars(MultipartFile file) {
        Map<String, Object> result = validateCars(file);

        if (!Boolean.TRUE.equals(result.get("valid"))) {
            throw new IllegalArgumentException(
                    "CSV содержит ошибки: " + result.get("errors")
            );
        }

        List<Car> cars = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(
                file.getInputStream(),
                StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
        )) {
            for (CSVRecord row : parser) {
                Car car = new Car(
                        null,
                        row.get("Марка"),
                        row.get("Модель"),
                        CarTransmission.valueOf(row.get("КПП").trim()),
                        Integer.parseInt(row.get("Год").trim()),
                        row.get("Госномер"),
                        CarBodyType.valueOf(row.get("Кузов").trim()),
                        CarStatus.valueOf(row.get("Статус").trim()),
                        Integer.parseInt(row.get("Пробег").trim())
                );

                cars.add(car);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Не удалось прочитать CSV.", e
            );
        }

        carRepository.saveAll(cars);

        return cars.size();
    }
}
