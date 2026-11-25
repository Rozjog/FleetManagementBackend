package ru.mirea.project.auth.repositories;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.mirea.project.auth.models.RefreshSession;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository
        extends JpaRepository<RefreshSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshSession> findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
            DELETE FROM RefreshSession s
            WHERE s.tokenHash = :tokenHash
            """)
    void deleteByTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query("""
            DELETE FROM RefreshSession s
            WHERE s.accountId = :accountId
            """)
    void deleteByAccountId(
            @Param("accountId") UUID accountId
    );
}
