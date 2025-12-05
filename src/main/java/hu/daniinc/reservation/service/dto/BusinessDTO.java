package hu.daniinc.reservation.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.Business} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BusinessDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private ZonedDateTime createdDate;

    @Size(max = 500)
    private String description;

    private String address;

    private String phoneNumber;

    @Min(value = 0)
    private Integer breakBetweenAppointmentsMin;

    private String logo;

    private String bannerUrl;

    private UserDTO owner;

    private Boolean appointmentApprovalRequired;

    private Integer maxWeeksInAdvance;

    private BusinessTheme theme;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getBreakBetweenAppointmentsMin() {
        return breakBetweenAppointmentsMin;
    }

    public void setBreakBetweenAppointmentsMin(Integer breakBetweenAppointmentsMin) {
        this.breakBetweenAppointmentsMin = breakBetweenAppointmentsMin;
    }

    public BusinessTheme getTheme() {
        return theme;
    }

    public void setTheme(BusinessTheme theme) {
        this.theme = theme;
    }

    public Integer getMaxWeeksInAdvance() {
        return maxWeeksInAdvance;
    }

    public void setMaxWeeksInAdvance(Integer maxWeeksInAdvance) {
        this.maxWeeksInAdvance = maxWeeksInAdvance;
    }

    public Boolean getAppointmentApprovalRequired() {
        return appointmentApprovalRequired;
    }

    public void setAppointmentApprovalRequired(Boolean appointmentApprovalRequired) {
        this.appointmentApprovalRequired = appointmentApprovalRequired;
    }

    private Set<WorkingHoursDTO> workingHours;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO user) {
        this.owner = user;
    }

    public Set<WorkingHoursDTO> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Set<WorkingHoursDTO> workingHours) {
        this.workingHours = workingHours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusinessDTO)) {
            return false;
        }

        BusinessDTO businessDTO = (BusinessDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, businessDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BusinessDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", description='" + getDescription() + "'" +
            ", address='" + getAddress() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", breakBetweenAppointmentsMin=" + getBreakBetweenAppointmentsMin() +
            ", logo='" + getLogo() + "'" +
            ", bannerUrl='" + getBannerUrl() + "'" +
            ", owner=" + getOwner() +
            "}";
    }
}
