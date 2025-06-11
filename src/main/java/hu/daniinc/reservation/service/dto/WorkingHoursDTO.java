package hu.daniinc.reservation.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.WorkingHours} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WorkingHoursDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer dayOfWeek;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkingHoursDTO)) {
            return false;
        }

        WorkingHoursDTO workingHoursDTO = (WorkingHoursDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, workingHoursDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WorkingHoursDTO{" +
            "id=" + getId() +
            ", dayOfWeek=" + getDayOfWeek() +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            "}";
    }
}
