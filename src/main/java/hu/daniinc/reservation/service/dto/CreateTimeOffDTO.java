package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.enumeration.TimeOffType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class CreateTimeOffDTO {

    private Long id;

    @NotNull
    private Long businessEmployeeId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @NotNull
    private TimeOffType type;

    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessEmployeeId() {
        return businessEmployeeId;
    }

    public void setBusinessEmployeeId(Long businessEmployeeId) {
        this.businessEmployeeId = businessEmployeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public TimeOffType getType() {
        return type;
    }

    public void setType(TimeOffType type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CreateTimeOffDTO that = (CreateTimeOffDTO) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(businessEmployeeId, that.businessEmployeeId) &&
            Objects.equals(startDate, that.startDate) &&
            Objects.equals(endDate, that.endDate) &&
            Objects.equals(startTime, that.startTime) &&
            Objects.equals(endTime, that.endTime) &&
            type == that.type &&
            Objects.equals(note, that.note)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessEmployeeId, startDate, endDate, startTime, endTime, type, note);
    }
}
