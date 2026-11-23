package ru.mirea.project.dto;

import ru.mirea.project.enums.Priority;
import ru.mirea.project.enums.RequestStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RentalRequestDTO {

    private UUID id;
    private Long carId;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer monthlyPrice;
    private RequestStatus status;
    private Priority priority;
    private Boolean isLongTerm;
    private Boolean archived;
    private String notes;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getCarId() { return carId; }

    public void setCarId(Long carId) { this.carId = carId; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(Integer monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Boolean getIsLongTerm() { return isLongTerm; }
    public void setIsLongTerm(Boolean isLongTerm) { this.isLongTerm = isLongTerm; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}