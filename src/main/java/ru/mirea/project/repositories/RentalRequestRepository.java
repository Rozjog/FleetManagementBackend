package ru.mirea.project.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mirea.project.enums.Priority;
import ru.mirea.project.enums.RequestStatus;
import ru.mirea.project.models.RentalRequest;

import java.time.LocalDate;
import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {

    Page<RentalRequest> findByArchivedFalse(Pageable pageable);

    Page<RentalRequest> findByStatusAndArchivedFalse(RequestStatus status, Pageable pageable);

    boolean existsByExternalId(String externalId);

    @Query("""
        SELECT r FROM RentalRequest r
        WHERE r.archived = false
          AND (:status IS NULL OR r.status = :status)
          AND (:priority IS NULL OR r.priority = :priority)
          AND (:from IS NULL OR r.startDate >= :from)
          AND (:to IS NULL OR r.endDate <= :to)
        """)
    Page<RentalRequest> search(RequestStatus status, Priority priority,
                               LocalDate from, LocalDate to, Pageable pageable);

    long countByStatusAndArchivedFalse(RequestStatus status);
}