package hu.daniinc.reservation.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.BusinessOpeningHours} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BusinessOpeningHoursDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer dayOfWeek;

    @NotNull
    private ZonedDateTime startTime;

    @NotNull
    private ZonedDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusinessOpeningHoursDTO)) {
            return false;
        }

        BusinessOpeningHoursDTO businessOpeningHoursDTO = (BusinessOpeningHoursDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, businessOpeningHoursDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BusinessOpeningHoursDTO{" +
            "id=" + getId() +
            ", dayOfWeek=" + getDayOfWeek() +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            "}";
    }
}
