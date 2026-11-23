package ru.mirea.project.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.mirea.project.enums.Priority;

import java.time.LocalDate;
import java.util.UUID;

public class RentalRequestCreateRequestDTO {

    private String externalId;
    private UUID carId;
    private UUID userId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @Future
    private LocalDate endDate;

    @NotNull
    @Positive
    private Integer monthlyPrice;

    private Priority priority = Priority.NORMAL;
    private String notes;

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public UUID getCarId() { return carId; }
    public void setCarId(UUID carId) { this.carId = carId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(Integer monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}