package ru.mirea.project.user.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.project.user.dto.UserRequest;
import ru.mirea.project.user.enums.UserStatus;
import ru.mirea.project.user.models.User;
import ru.mirea.project.user.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<User> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort
    ) {
        String[] sortParts = sort.split(",", -1);

        if (sortParts.length != 2) {
            throw new IllegalArgumentException(
                    "Сортировка должна быть в формате: поле,направление."
            );
        }

        return userService.searchUsers(
                search,
                status,
                page,
                size,
                sortParts[0].trim(),
                sortParts[1].trim(),
                archived
        );
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserRequest request
    ) {
        User createdUser = userService.createUser(toUser(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        return userService.updateUser(id, toUser(request));
    }

    @PatchMapping("/{id}/archive")
    public User archiveUser(@PathVariable Long id) {
        return userService.archiveUser(id);
    }

    @PatchMapping("/{id}/restore")
    public User restoreUser(@PathVariable Long id) {
        return userService.restoreUser(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    private User toUser(UserRequest request) {
        User user = new User();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setDriverLicense(request.driverLicense());
        user.setStatus(request.status());

        return user;
    }
}
