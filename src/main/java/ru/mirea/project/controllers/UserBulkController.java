package ru.mirea.project.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.mirea.project.dto.UserBulkStatusRequest;
import ru.mirea.project.services.UserBulkService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserBulkController {

    private final UserBulkService userBulkService;

    public UserBulkController(UserBulkService userBulkService) {
        this.userBulkService = userBulkService;
    }

    @PatchMapping("/bulk/status")
    public Map<String, Object> changeStatus(
            @Valid @RequestBody UserBulkStatusRequest request
    ) {
        int updated = userBulkService.changeStatus(request);

        return Map.of(
                "updated", updated,
                "status", request.status()
        );
    }
}
