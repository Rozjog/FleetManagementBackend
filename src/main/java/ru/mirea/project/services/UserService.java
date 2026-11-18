package ru.mirea.project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.project.enums.UserStatus;
import ru.mirea.project.models.User;
import ru.mirea.project.repositories.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

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

    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Данные пользователя отсутствуют.");
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

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User data) {
        User user = getUserById(id);

        if (data == null) {
            throw new IllegalArgumentException("Данные пользователя отсутствуют.");
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
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
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
            throw new IllegalArgumentException("Некорректное имя пользователя.");
        }

        if (lastName.isEmpty() || lastName.length() > 100) {
            throw new IllegalArgumentException("Некорректная фамилия пользователя.");
        }

        if (!phone.matches("\\+7\\d{10}")) {
            throw new IllegalArgumentException(
                    "Телефон должен быть в формате +79991234567."
            );
        }

        if (!email.isEmpty() &&
                (email.length() > 255 ||
                        !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            throw new IllegalArgumentException("Некорректный email.");
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
