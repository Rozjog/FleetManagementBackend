package ru.mirea.project.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.project.enums.UserStatus;
import ru.mirea.project.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserCsvService {

    private static final List<String> HEADERS = List.of(
            "firstName",
            "lastName",
            "phone",
            "email",
            "driverLicense",
            "status"
    );

    private final UserService userService;

    public UserCsvService(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    public int importUsers(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "CSV-файл пустой или отсутствует."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IllegalArgumentException(
                    "Необходимо загрузить файл с расширением .csv."
            );
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .get();

        Set<String> phones = new HashSet<>();
        Set<String> emails = new HashSet<>();
        Set<String> licenses = new HashSet<>();

        int imported = 0;

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

            try (CSVParser parser = format.parse(reader)) {

                if (!parser.getHeaderNames().equals(HEADERS)) {
                    throw new IllegalArgumentException(
                            "Неверные заголовки CSV. Ожидаются: " +
                                    String.join(",", HEADERS)
                    );
                }

                for (CSVRecord record : parser) {

                    long row = record.getRecordNumber() + 1;

                    if (imported >= 1000) {
                        throw new IllegalArgumentException(
                                "За один раз можно импортировать максимум 1000 пользователей."
                        );
                    }

                    if (!record.isConsistent()) {
                        throw new IllegalArgumentException(
                                "Запись CSV №" + row +
                                        ": неправильное количество полей."
                        );
                    }

                    User user = new User();

                    user.setFirstName(record.get("firstName"));
                    user.setLastName(record.get("lastName"));
                    user.setPhone(record.get("phone"));
                    user.setEmail(record.get("email"));
                    user.setDriverLicense(record.get("driverLicense"));

                    String status = record.get("status").trim();

                    if (!status.isEmpty()) {
                        try {
                            user.setStatus(UserStatus.valueOf(
                                    status.toUpperCase(Locale.ROOT)
                            ));
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(
                                    "Запись CSV №" + row +
                                            ": неизвестный статус " + status
                            );
                        }
                    }

                    checkDuplicate(
                            phones,
                            user.getPhone().trim(),
                            "телефон",
                            row
                    );

                    checkDuplicate(
                            emails,
                            user.getEmail().trim().toLowerCase(Locale.ROOT),
                            "email",
                            row
                    );

                    checkDuplicate(
                            licenses,
                            user.getDriverLicense().trim(),
                            "водительское удостоверение",
                            row
                    );

                    try {
                        userService.createUser(user);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                "Запись CSV №" + row + ": " + e.getMessage(),
                                e
                        );
                    }

                    imported++;
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Ошибка чтения CSV-файла.", e
            );
        }

        if (imported == 0) {
            throw new IllegalArgumentException(
                    "CSV-файл не содержит пользователей."
            );
        }

        return imported;
    }

    private void checkDuplicate(
            Set<String> values,
            String value,
            String field,
            long row
    ) {
        if (!value.isEmpty() && !values.add(value)) {
            throw new IllegalArgumentException(
                    "Запись CSV №" + row +
                            ": повторяется " + field + " — " + value
            );
        }
    }
}
