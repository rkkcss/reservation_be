package hu.daniinc.reservation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Business.
 */
@Entity
@Table(name = "business")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Business implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_date")
    private ZonedDateTime createdDate;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Min(value = 0)
    @Column(name = "break_between_appointments_min")
    private Integer breakBetweenAppointmentsMin;

    @Column(name = "logo")
    private String logo;

    @Column(name = "banner_url")
    private String bannerUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private User user;

    @JsonIgnoreProperties(value = { "guest", "business", "offerings" }, allowSetters = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "business")
    private Set<Appointment> appointments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "business")
    @JsonIgnoreProperties(value = { "business" }, allowSetters = true)
    private Set<WorkingHours> workingHours = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "business")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "business" }, allowSetters = true)
    private Set<CustomWorkingHours> customWorkingHours = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "business")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "business", "appointment" }, allowSetters = true)
    private Set<Offering> offerings = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "business")
    @JsonIgnoreProperties(value = { "business" }, allowSetters = true)
    private Set<Guest> guests = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Business id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Business name(String name) {
        this.setName(name);
        return this;
    }

    public Set<Guest> getGuests() {
        return guests;
    }

    public void setGuests(Set<Guest> guests) {
        this.guests = guests;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getCreatedDate() {
        return this.createdDate;
    }

    public Business createdDate(ZonedDateTime createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getDescription() {
        return this.description;
    }

    public Business description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return this.address;
    }

    public Business address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public Business phoneNumber(String phoneNumber) {
        this.setPhoneNumber(phoneNumber);
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getBreakBetweenAppointmentsMin() {
        return this.breakBetweenAppointmentsMin;
    }

    public Business breakBetweenAppointmentsMin(Integer breakBetweenAppointmentsMin) {
        this.setBreakBetweenAppointmentsMin(breakBetweenAppointmentsMin);
        return this;
    }

    public void setBreakBetweenAppointmentsMin(Integer breakBetweenAppointmentsMin) {
        this.breakBetweenAppointmentsMin = breakBetweenAppointmentsMin;
    }

    public String getLogo() {
        return this.logo;
    }

    public Business logo(String logo) {
        this.setLogo(logo);
        return this;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBannerUrl() {
        return this.bannerUrl;
    }

    public Business bannerUrl(String bannerUrl) {
        this.setBannerUrl(bannerUrl);
        return this;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Business user(User user) {
        this.setUser(user);
        return this;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<Appointment> appointments) {
        this.appointments = appointments;
    }

    public Set<WorkingHours> getWorkingHours() {
        return this.workingHours;
    }

    public void setWorkingHours(Set<WorkingHours> workingHours) {
        if (this.workingHours != null) {
            this.workingHours.forEach(i -> i.setBusiness(null));
        }
        if (workingHours != null) {
            workingHours.forEach(i -> i.setBusiness(this));
        }
        this.workingHours = workingHours;
    }

    public Business workingHours(Set<WorkingHours> workingHours) {
        this.setWorkingHours(workingHours);
        return this;
    }

    public Business addWorkingHours(WorkingHours workingHours) {
        this.workingHours.add(workingHours);
        workingHours.setBusiness(this);
        return this;
    }

    public Business removeWorkingHours(WorkingHours workingHours) {
        this.workingHours.remove(workingHours);
        workingHours.setBusiness(null);
        return this;
    }

    public Set<CustomWorkingHours> getCustomWorkingHours() {
        return this.customWorkingHours;
    }

    public void setCustomWorkingHours(Set<CustomWorkingHours> customWorkingHours) {
        if (this.customWorkingHours != null) {
            this.customWorkingHours.forEach(i -> i.setBusiness(null));
        }
        if (customWorkingHours != null) {
            customWorkingHours.forEach(i -> i.setBusiness(this));
        }
        this.customWorkingHours = customWorkingHours;
    }

    public Business customWorkingHours(Set<CustomWorkingHours> customWorkingHours) {
        this.setCustomWorkingHours(customWorkingHours);
        return this;
    }

    public Business addCustomWorkingHours(CustomWorkingHours customWorkingHours) {
        this.customWorkingHours.add(customWorkingHours);
        customWorkingHours.setBusiness(this);
        return this;
    }

    public Business removeCustomWorkingHours(CustomWorkingHours customWorkingHours) {
        this.customWorkingHours.remove(customWorkingHours);
        customWorkingHours.setBusiness(null);
        return this;
    }

    public Set<Offering> getOfferings() {
        return this.offerings;
    }

    public void setOfferings(Set<Offering> offerings) {
        if (this.offerings != null) {
            this.offerings.forEach(i -> i.setBusiness(null));
        }
        if (offerings != null) {
            offerings.forEach(i -> i.setBusiness(this));
        }
        this.offerings = offerings;
    }

    public Business offerings(Set<Offering> offerings) {
        this.setOfferings(offerings);
        return this;
    }

    public Business addOffering(Offering offering) {
        this.offerings.add(offering);
        offering.setBusiness(this);
        return this;
    }

    public Business removeOffering(Offering offering) {
        this.offerings.remove(offering);
        offering.setBusiness(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Business)) {
            return false;
        }
        return getId() != null && getId().equals(((Business) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Business{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", description='" + getDescription() + "'" +
            ", address='" + getAddress() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", breakBetweenAppointmentsMin=" + getBreakBetweenAppointmentsMin() +
            ", logo='" + getLogo() + "'" +
            ", bannerUrl='" + getBannerUrl() + "'" +
            "}";
    }

    public void appointments(Object o) {}
}
