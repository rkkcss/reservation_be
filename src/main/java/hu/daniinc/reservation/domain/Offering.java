package hu.daniinc.reservation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.daniinc.reservation.domain.enumeration.AppointmentStatus;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLRestriction;

/**
 * A Offering.
 */
@Entity
@Table(name = "offering")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SQLRestriction("status <> 'DELETED'")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Offering implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Min(value = 1)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @NotNull
    @DecimalMin(value = "1")
    @Column(name = "price", precision = 21, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "business", "user", "appointments", "offering" }, allowSetters = true)
    private BusinessEmployee businessEmployee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BasicEntityStatus status = BasicEntityStatus.INACTIVE;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Offering id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDurationMinutes() {
        return this.durationMinutes;
    }

    public Offering durationMinutes(Integer durationMinutes) {
        this.setDurationMinutes(durationMinutes);
        return this;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Offering price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return this.description;
    }

    public Offering description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public Offering title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BusinessEmployee getBusinessEmployee() {
        return this.businessEmployee;
    }

    public void setBusinessEmployee(BusinessEmployee businessEmployee) {
        this.businessEmployee = businessEmployee;
    }

    public Offering businessEmployee(BusinessEmployee businessEmployee) {
        this.setBusinessEmployee(businessEmployee);
        return this;
    }

    public BasicEntityStatus getStatus() {
        return status;
    }

    public void setStatus(BasicEntityStatus status) {
        this.status = status;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Offering)) {
            return false;
        }
        return getId() != null && getId().equals(((Offering) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Offering{" +
            "id=" + getId() +
            ", durationMinutes=" + getDurationMinutes() +
            ", price=" + getPrice() +
            ", description='" + getDescription() + "'" +
            ", title='" + getTitle() + "'" +
            "}";
    }
}
