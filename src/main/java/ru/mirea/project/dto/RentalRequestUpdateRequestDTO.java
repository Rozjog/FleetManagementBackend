package ru.mirea.project.dto;

import jakarta.validation.constraints.Positive;
import ru.mirea.project.enums.Priority;
import ru.mirea.project.enums.RequestStatus;

import java.time.LocalDate;

public class RentalRequestUpdateRequestDTO {

    private LocalDate startDate;
    private LocalDate endDate;

    @Positive
    private Integer monthlyPrice;

    private RequestStatus status;
    private Priority priority;
    private String notes;
    private Long version;

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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}