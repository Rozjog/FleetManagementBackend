package ru.mirea.project.user.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.mirea.project.user.enums.UserStatus;
import ru.mirea.project.user.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends
        JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByDriverLicense(String driverLicense);

    List<User> findByStatus(UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    long countByArchivedFalse();

    long countByArchivedTrue();

    long countByStatusAndArchivedFalse(UserStatus status);

    @Query(value = """
        SELECT COUNT(DISTINCT r.user_id)
        FROM rental_request r
        JOIN users u ON r.user_id = u.user_id
        WHERE u.archived = false
          AND r.archived = false
        """, nativeQuery = true)
    long countUsersWithRequests();

    @Query(value = """
        SELECT COUNT(*)
        FROM rental_request r
        JOIN users u ON r.user_id = u.user_id
        WHERE u.archived = false
          AND r.archived = false
        """, nativeQuery = true)
    long countUserRequests();

}
