package ru.mirea.project.request.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.mirea.project.request.enums.Priority;

import java.time.LocalDate;

public class RentalRequestCreateRequestDTO {

    private String externalId;

    @NotNull
    private Long carId;

    @NotNull
    private Long userId;

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

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}