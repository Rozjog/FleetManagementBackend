package ru.mirea.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.project.enums.UserStatus;
import ru.mirea.project.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByDriverLicense(String driverLicense);

    List<User> findByStatus(UserStatus status);
}
