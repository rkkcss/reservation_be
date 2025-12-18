package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class BusinessEmployeeDTO {

    private Long id;

    private Business business;

    private UserDTO user;

    private BusinessRole role;

    private Set<BusinessPermission> permissions = new HashSet<>();

    private Instant createdDate;

    private Instant modifiedDate;

    private String createdBy;

    private BasicEntityStatus status;

    private String phoneNumber;

    private Set<AppointmentDTO> appointments = new HashSet<>();

    private Set<WorkingHoursDTO> workingHours = new HashSet<>();

    private Set<GuestDTO> guests = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public BusinessRole getRole() {
        return role;
    }

    public void setRole(BusinessRole role) {
        this.role = role;
    }

    public Set<BusinessPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<BusinessPermission> permissions) {
        this.permissions = permissions;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public BasicEntityStatus getStatus() {
        return status;
    }

    public void setStatus(BasicEntityStatus status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<AppointmentDTO> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<AppointmentDTO> appointments) {
        this.appointments = appointments;
    }

    public Set<WorkingHoursDTO> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Set<WorkingHoursDTO> workingHours) {
        this.workingHours = workingHours;
    }

    public Set<GuestDTO> getGuests() {
        return guests;
    }

    public void setGuests(Set<GuestDTO> guests) {
        this.guests = guests;
    }
}
