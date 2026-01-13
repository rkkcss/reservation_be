package hu.daniinc.reservation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedDate;

/**
 * A Guest.
 */
@Entity
@Table(name = "guest")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Guest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "can_book", nullable = false)
    private boolean canBook;

    @JsonIgnoreProperties(value = { "guest", "business", "offerings" }, allowSetters = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "guest")
    private Set<Appointment> appointments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "guest")
    @JsonIgnoreProperties(value = { "guest" })
    private Set<BusinessRating> businessRating = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessEmployee businessEmployee;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Long getId() {
        return this.id;
    }

    public Guest id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Guest name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public Guest email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public Guest phoneNumber(String phoneNumber) {
        this.setPhoneNumber(phoneNumber);
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<Appointment> appointments) {
        this.appointments = appointments;
    }

    public Boolean getCanBook() {
        return canBook;
    }

    public void setCanBook(Boolean canBook) {
        this.canBook = canBook;
    }

    public Set<BusinessRating> getBusinessRating() {
        return businessRating;
    }

    public void setBusinessRating(Set<BusinessRating> businessRating) {
        this.businessRating = businessRating;
    }

    public BusinessEmployee getBusinessEmployee() {
        return businessEmployee;
    }

    public void setBusinessEmployee(BusinessEmployee businessEmployee) {
        this.businessEmployee = businessEmployee;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Guest)) {
            return false;
        }
        return getId() != null && getId().equals(((Guest) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Guest{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", email='" + getEmail() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            "}";
    }
}
