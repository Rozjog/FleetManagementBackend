package ru.mirea.project.user.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.project.user.enums.UserStatus;
import ru.mirea.project.user.models.User;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class UserCsvExportService {

    private final UserService userService;

    public UserCsvExportService(UserService userService) {
        this.userService = userService;
    }

    public byte[] exportUsers(String search, UserStatus status) {

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(
                        "id",
                        "firstName",
                        "lastName",
                        "phone",
                        "email",
                        "driverLicense",
                        "status"
                )
                .get();

        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {

            int page = 0;
            Page<User> result;

            do {
                result = userService.searchUsers(
                        search,
                        status,
                        page,
                        100,
                        "id",
                        "asc"
                );

                for (User user : result.getContent()) {

                    printer.printRecord(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getPhone(),
                            user.getEmail(),
                            user.getDriverLicense(),
                            user.getStatus().name()
                    );
                }

                page++;

            } while (result.hasNext());

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Ошибка формирования CSV-файла.", e
            );
        }

        return ("\uFEFF" + writer)
                .getBytes(StandardCharsets.UTF_8);
    }
}
