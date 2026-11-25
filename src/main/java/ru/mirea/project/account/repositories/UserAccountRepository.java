package ru.mirea.project.account.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.project.account.models.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
