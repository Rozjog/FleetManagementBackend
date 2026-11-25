
package ru.mirea.project.user.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.project.user.enums.UserStatus;
import ru.mirea.project.user.enums.UserImportMode;
import ru.mirea.project.user.services.UserCsvExportService;
import ru.mirea.project.user.services.UserCsvService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserCsvController {

    private final UserCsvService userCsvService;
    private final UserCsvExportService userCsvExportService;

    public UserCsvController(
            UserCsvService userCsvService,
            UserCsvExportService userCsvExportService
    ) {
        this.userCsvService = userCsvService;
        this.userCsvExportService = userCsvExportService;
    }

    @PostMapping(
            value = "/import/validate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> validateImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "CREATE_ONLY")
            UserImportMode mode
    ) {
        return userCsvService.validateUsers(file, mode);
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Integer> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "CREATE_ONLY")
            UserImportMode mode
    ) {
        int imported = userCsvService.importUsers(file, mode);

        return Map.of("imported", imported);
    }

    @GetMapping(
            value = "/export",
            produces = "text/csv"
    )
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status
    ) {
        byte[] csv = userCsvExportService.exportUsers(
                search,
                status
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"users.csv\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "text/csv;charset=UTF-8"
                        )
                )
                .body(csv);
    }
}
