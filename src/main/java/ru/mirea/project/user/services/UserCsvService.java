package ru.mirea.project.user.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.project.user.enums.UserImportMode;
import ru.mirea.project.user.enums.UserStatus;
import ru.mirea.project.user.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    private static final List<String> HEADERS_WITH_ID = List.of(
            "id",
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

    // Предварительная проверка CSV
    public Map<String, Object> validateUsers(
            MultipartFile file,
            UserImportMode mode
    ) {
        ImportPlan plan = readAndValidate(file, mode);

        return Map.of(
                "valid", plan.errors().isEmpty(),
                "totalRows", plan.totalRows(),
                "validRows", plan.rows().size(),
                "invalidRows", plan.totalRows() - plan.rows().size(),
                "errors", plan.errors()
        );
    }

    // Импорт CSV в PostgreSQL
    @Transactional
    public int importUsers(
            MultipartFile file,
            UserImportMode mode
    ) {
        ImportPlan plan = readAndValidate(file, mode);

        if (!plan.errors().isEmpty()) {
            throw new IllegalArgumentException(
                    String.join("; ", plan.errors())
            );
        }

        for (ImportRow row : plan.rows()) {
            if (mode == UserImportMode.CREATE_ONLY) {
                userService.createUser(row.user());
            } else {
                userService.updateUser(row.id(), row.user());
            }
        }

        return plan.rows().size();
    }

    // Чтение и проверка всех записей CSV
    private ImportPlan readAndValidate(
            MultipartFile file,
            UserImportMode mode
    ) {
        validateFile(file);

        Set<Long> ids = new HashSet<>();
        Set<String> phones = new HashSet<>();
        Set<String> emails = new HashSet<>();
        Set<String> licenses = new HashSet<>();

        List<ImportRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int totalRows = 0;

        try (BufferedReader reader = createReader(file);
             CSVParser parser = createFormat().parse(reader)) {

            List<String> headers = parser.getHeaderNames();

            boolean hasId = headers.equals(HEADERS_WITH_ID);

            if (mode == UserImportMode.UPDATE_BY_ID && !hasId) {
                errors.add(
                        "Для обновления необходим столбец id."
                );

                return new ImportPlan(0, rows, errors);
            }

            if (!hasId && !headers.equals(HEADERS)) {
                errors.add("Некорректные заголовки CSV.");

                return new ImportPlan(0, rows, errors);
            }

            for (CSVRecord record : parser) {

                totalRows++;

                if (totalRows > 1000) {
                    throw new IllegalArgumentException(
                            "Максимум 1000 пользователей в CSV."
                    );
                }

                long rowNumber = record.getRecordNumber() + 1;

                try {
                    if (!record.isConsistent()) {
                        throw new IllegalArgumentException(
                                "Неправильное количество полей."
                        );
                    }

                    User user = parseUser(record);
                    Long id = null;

                    if (mode == UserImportMode.UPDATE_BY_ID) {

                        id = parseId(record.get("id"));

                        if (ids.contains(id)) {
                            throw new IllegalArgumentException(
                                    "Повторяется ID: " + id
                            );
                        }

                        userService.validateForUpdate(id, user);

                    } else {

                        if (hasId && !record.get("id").isBlank()) {
                            throw new IllegalArgumentException(
                                    "При создании ID должен быть пустым."
                            );
                        }

                        userService.validateForCreation(user);
                    }

                    checkDuplicate(
                            phones,
                            user.getPhone(),
                            "телефон"
                    );

                    checkDuplicate(
                            emails,
                            user.getEmail(),
                            "email"
                    );

                    checkDuplicate(
                            licenses,
                            user.getDriverLicense(),
                            "водительское удостоверение"
                    );

                    if (id != null) {
                        ids.add(id);
                    }

                    phones.add(user.getPhone());

                    if (user.getEmail() != null) {
                        emails.add(user.getEmail());
                    }

                    if (user.getDriverLicense() != null) {
                        licenses.add(user.getDriverLicense());
                    }

                    rows.add(new ImportRow(id, user));

                } catch (IllegalArgumentException |
                         NoSuchElementException e) {

                    errors.add(
                            "Запись CSV №" + rowNumber +
                                    ": " + e.getMessage()
                    );
                }
            }

        } catch (IOException | UncheckedIOException e) {
            throw new IllegalArgumentException(
                    "Ошибка чтения CSV-файла.", e
            );
        }

        if (totalRows == 0) {
            errors.add("CSV-файл не содержит пользователей.");
        }

        return new ImportPlan(totalRows, rows, errors);
    }

    // Преобразование строки CSV в объект User
    private User parseUser(CSVRecord record) {

        User user = new User();

        user.setFirstName(record.get("firstName"));
        user.setLastName(record.get("lastName"));
        user.setPhone(record.get("phone"));
        user.setEmail(record.get("email"));
        user.setDriverLicense(record.get("driverLicense"));

        String status = record.get("status").trim();

        if (status.isEmpty()) {
            user.setStatus(UserStatus.REGULAR);
        } else {
            try {
                user.setStatus(
                        UserStatus.valueOf(
                                status.toUpperCase(Locale.ROOT)
                        )
                );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Неизвестный статус: " + status
                );
            }
        }

        return user;
    }

    // Проверка ID пользователя
    private Long parseId(String value) {

        try {
            long id = Long.parseLong(value.trim());

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "ID должен быть положительным."
                );
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некорректный ID пользователя."
            );
        }
    }

    // Проверка дубликатов внутри CSV
    private void checkDuplicate(
            Set<String> values,
            String value,
            String field
    ) {
        if (value != null && values.contains(value)) {
            throw new IllegalArgumentException(
                    "Повторяется " + field + ": " + value
            );
        }
    }

    // Проверка файла
    private void validateFile(MultipartFile file) {

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
    }

    // Настройка CSV-парсера
    private CSVFormat createFormat() {

        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .get();
    }

    // Чтение UTF-8 с поддержкой BOM
    private BufferedReader createReader(
            MultipartFile file
    ) throws IOException {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

        reader.mark(1);

        if (reader.read() != '\uFEFF') {
            reader.reset();
        }

        return reader;
    }

    private record ImportRow(
            Long id,
            User user
    ) {}

    private record ImportPlan(
            int totalRows,
            List<ImportRow> rows,
            List<String> errors
    ) {}
}
