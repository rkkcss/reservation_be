package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.BusinessOpeningHours;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.BusinessOpeningHours} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BusinessOpeningHoursDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private Set<BusinessOpeningHoursDTO> openingHours = new HashSet<>();

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

    public Set<BusinessOpeningHoursDTO> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(Set<BusinessOpeningHoursDTO> openingHours) {
        this.openingHours = openingHours;
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
