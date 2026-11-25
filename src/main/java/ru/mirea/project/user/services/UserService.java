package ru.mirea.project.user.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.project.user.enums.UserStatus;
import ru.mirea.project.user.models.User;
import ru.mirea.project.user.repositories.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "firstName", "lastName", "phone",
            "email", "driverLicense", "status", "archived"
    );

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь с ID " + id + " не найден."
                ));
    }

    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    public Page<User> searchUsers(
            String search,
            UserStatus status,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        return searchUsers(
                search, status, page, size,
                sortBy, direction, false
        );
    }

    public Page<User> searchUsers(
            String search,
            UserStatus status,
            int page,
            int size,
            String sortBy,
            String direction,
            boolean archived
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Номер страницы не может быть отрицательным."
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Размер страницы должен быть от 1 до 100."
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Недопустимое поле сортировки: " + sortBy
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException(
                    "Направление сортировки: asc или desc."
            );
        }

        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Specification<User> specification =
                (root, query, cb) ->
                        cb.equal(root.get("archived"), archived);

        if (search != null && !search.isBlank()) {
            String value = "%" +
                    search.trim().toLowerCase(Locale.ROOT)
                            .replace("\\", "\\\\")
                            .replace("%", "\\%")
                            .replace("_", "\\_")
                    + "%";

            specification = specification.and(
                    (root, query, cb) -> cb.or(
                            cb.like(
                                    cb.lower(root.get("firstName")),
                                    value, '\\'
                            ),
                            cb.like(
                                    cb.lower(root.get("lastName")),
                                    value, '\\'
                            ),
                            cb.like(
                                    cb.lower(root.get("phone")),
                                    value, '\\'
                            ),
                            cb.like(
                                    cb.lower(root.get("email")),
                                    value, '\\'
                            )
                    )
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("status"), status)
            );
        }

        return userRepository.findAll(specification, pageable);
    }

    public void validateForCreation(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "Данные пользователя отсутствуют."
            );
        }

        if (user.getId() != null) {
            throw new IllegalArgumentException(
                    "При создании ID указывать не нужно."
            );
        }

        validateUser(user);

        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException(
                    "Пользователь с таким телефоном уже существует."
            );
        }

        if (user.getEmail() != null &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Пользователь с таким email уже существует."
            );
        }

        if (user.getDriverLicense() != null &&
                userRepository.existsByDriverLicense(user.getDriverLicense())) {
            throw new IllegalArgumentException(
                    "Пользователь с таким удостоверением уже существует."
            );
        }
    }

    @Transactional
    public User createUser(User user) {
        validateForCreation(user);
        return userRepository.save(user);
    }


    @Transactional
    public User updateUser(Long id, User data) {
        User user = getUserById(id);

        if (data == null) {
            throw new IllegalArgumentException(
                    "Данные пользователя отсутствуют."
            );
        }

        validateUser(data);

        if (!Objects.equals(user.getPhone(), data.getPhone()) &&
                userRepository.findByPhone(data.getPhone()).isPresent()) {
            throw new IllegalArgumentException(
                    "Пользователь с таким телефоном уже существует."
            );
        }

        if (!Objects.equals(user.getEmail(), data.getEmail()) &&
                data.getEmail() != null &&
                userRepository.existsByEmail(data.getEmail())) {
            throw new IllegalArgumentException(
                    "Пользователь с таким email уже существует."
            );
        }

        if (!Objects.equals(user.getDriverLicense(), data.getDriverLicense()) &&
                data.getDriverLicense() != null &&
                userRepository.existsByDriverLicense(data.getDriverLicense())) {
            throw new IllegalArgumentException(
                    "Пользователь с таким удостоверением уже существует."
            );
        }

        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        user.setDriverLicense(data.getDriverLicense());
        user.setStatus(data.getStatus());

        return userRepository.save(user);
    }

    @Transactional
    public User archiveUser(Long id) {
        User user = getUserById(id);
        user.setArchived(true);
        return userRepository.save(user);
    }

    @Transactional
    public User restoreUser(Long id) {
        User user = getUserById(id);
        user.setArchived(false);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        archiveUser(id);
    }

    private void validateUser(User user) {
        String firstName = user.getFirstName() == null
                ? "" : user.getFirstName().trim();

        String lastName = user.getLastName() == null
                ? "" : user.getLastName().trim();

        String phone = user.getPhone() == null
                ? "" : user.getPhone().trim();

        String email = user.getEmail() == null
                ? "" : user.getEmail().trim().toLowerCase(Locale.ROOT);

        String license = user.getDriverLicense() == null
                ? "" : user.getDriverLicense().trim();

        if (firstName.isEmpty() || firstName.length() > 100) {
            throw new IllegalArgumentException(
                    "Некорректное имя пользователя."
            );
        }

        if (lastName.isEmpty() || lastName.length() > 100) {
            throw new IllegalArgumentException(
                    "Некорректная фамилия пользователя."
            );
        }

        if (!phone.matches("\\+7\\d{10}")) {
            throw new IllegalArgumentException(
                    "Телефон должен быть в формате +79991234567."
            );
        }

        if (!email.isEmpty() &&
                (email.length() > 255 ||
                        !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            throw new IllegalArgumentException(
                    "Некорректный email."
            );
        }

        if (!license.isEmpty() && !license.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Водительское удостоверение должно содержать 10 цифр."
            );
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setEmail(email.isEmpty() ? null : email);
        user.setDriverLicense(license.isEmpty() ? null : license);

        if (user.getStatus() == null) {
            user.setStatus(UserStatus.REGULAR);
        }
    }
}
