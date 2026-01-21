package hu.daniinc.reservation.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.CustomWorkingHours} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CustomWorkingHoursDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate workDate;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    private BusinessDTO business;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public BusinessDTO getBusiness() {
        return business;
    }

    public void setBusiness(BusinessDTO business) {
        this.business = business;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomWorkingHoursDTO)) {
            return false;
        }

        CustomWorkingHoursDTO customWorkingHoursDTO = (CustomWorkingHoursDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, customWorkingHoursDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CustomWorkingHoursDTO{" +
            "id=" + getId() +
            ", workDate='" + getWorkDate() + "'" +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", business=" + getBusiness() +
            "}";
    }
}
