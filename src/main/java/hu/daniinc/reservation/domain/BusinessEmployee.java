package hu.daniinc.reservation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.BusinessPermissionHandler;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "business_employee", uniqueConstraints = { @UniqueConstraint(columnNames = { "business_id", "user_id" }) })
public class BusinessEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private BusinessRole role;

    @ElementCollection(targetClass = BusinessPermission.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "business_employee_permissions", joinColumns = @JoinColumn(name = "business_employee_id"))
    @Column(name = "permission")
    private Set<BusinessPermission> permissions = new HashSet<>();

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant modifiedDate;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    private BasicEntityStatus status = BasicEntityStatus.ACTIVE;

    @Column(length = 15, name = "phone_number")
    private String phoneNumber;

    @OneToMany(mappedBy = "businessEmployee", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Appointment> appointments = new HashSet<>();

    @OneToMany(mappedBy = "businessEmployee", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Offering> offerings = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "businessEmployee")
    @JsonIgnoreProperties(value = { "businessEmployee" }, allowSetters = true)
    private Set<WorkingHours> workingHours = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "businessEmployee")
    @JsonIgnoreProperties(value = { "businessEmployee" }, allowSetters = true)
    private Set<Guest> guests = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public boolean hasPermission(BusinessPermission permission) {
        return permissions.contains(permission);
    }

    public BusinessEmployee withRole(BusinessRole role) {
        this.role = role;
        return this;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<Appointment> appointments) {
        this.appointments = appointments;
    }

    public BusinessEmployee withPermission(BusinessPermission permission) {
        this.permissions.add(permission);
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<Offering> getOfferings() {
        return offerings;
    }

    public void setOfferings(Set<Offering> offerings) {
        this.offerings = offerings;
    }

    public Set<WorkingHours> getWorkingHours() {
        return this.workingHours;
    }

    public void setWorkingHours(Set<WorkingHours> workingHours) {
        if (this.workingHours != null) {
            this.workingHours.forEach(i -> i.setBusinessEmployee(null));
        }
        if (workingHours != null) {
            workingHours.forEach(i -> i.setBusinessEmployee(this));
        }
        this.workingHours = workingHours;
    }

    public BusinessEmployee workingHours(Set<WorkingHours> workingHours) {
        this.setWorkingHours(workingHours);
        return this;
    }

    public BusinessEmployee addWorkingHours(WorkingHours workingHours) {
        this.workingHours.add(workingHours);
        workingHours.setBusinessEmployee(this);
        return this;
    }

    public Set<Guest> getGuests() {
        return guests;
    }

    public void setGuests(Set<Guest> guests) {
        this.guests = guests;
    }

    //create owner

    public static BusinessEmployee owner(Business business, User user) {
        BusinessEmployee be = new BusinessEmployee();
        be.business = business;
        be.user = user;
        be.role = BusinessRole.OWNER;
        be.permissions = BusinessPermissionHandler.OWNER.getPermissions();
        return be;
    }
}
