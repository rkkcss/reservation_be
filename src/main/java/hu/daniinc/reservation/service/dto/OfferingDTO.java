package hu.daniinc.reservation.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.Offering} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OfferingDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(value = 1)
    private Integer durationMinutes;

    @NotNull
    @DecimalMin(value = "1")
    private BigDecimal price;

    private String description;

    @NotNull
    private String title;

    private BusinessDTO business;

    private AppointmentDTO appointment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BusinessDTO getBusiness() {
        return business;
    }

    public void setBusiness(BusinessDTO business) {
        this.business = business;
    }

    public AppointmentDTO getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentDTO appointment) {
        this.appointment = appointment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OfferingDTO)) {
            return false;
        }

        OfferingDTO offeringDTO = (OfferingDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, offeringDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OfferingDTO{" +
            "id=" + getId() +
            ", durationMinutes=" + getDurationMinutes() +
            ", price=" + getPrice() +
            ", description='" + getDescription() + "'" +
            ", title='" + getTitle() + "'" +
            ", business=" + getBusiness() +
            ", appointment=" + getAppointment() +
            "}";
    }
}
