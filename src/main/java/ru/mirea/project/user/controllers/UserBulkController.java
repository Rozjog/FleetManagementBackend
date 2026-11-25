package ru.mirea.project.user.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.mirea.project.user.dto.UserBulkRequest;
import ru.mirea.project.user.dto.UserBulkResponse;
import ru.mirea.project.user.dto.UserBulkStatusRequest;
import ru.mirea.project.user.services.UserBulkService;

@RestController
@RequestMapping("/api/users")
public class UserBulkController {

    private final UserBulkService userBulkService;

    public UserBulkController(UserBulkService userBulkService) {
        this.userBulkService = userBulkService;
    }

    @PatchMapping("/bulk/status")
    public UserBulkResponse changeStatus(
            @Valid @RequestBody UserBulkStatusRequest request
    ) {
        return userBulkService.changeStatus(request);
    }

    @PatchMapping("/bulk/archive")
    public UserBulkResponse archiveUsers(
            @Valid @RequestBody UserBulkRequest request
    ) {
        return userBulkService.archiveUsers(request);
    }

    @PatchMapping("/bulk/restore")
    public UserBulkResponse restoreUsers(
            @Valid @RequestBody UserBulkRequest request
    ) {
        return userBulkService.restoreUsers(request);
    }
}
