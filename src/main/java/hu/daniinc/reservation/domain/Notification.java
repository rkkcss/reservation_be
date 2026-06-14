package hu.daniinc.reservation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private String type; // "appointment.cancelled", "employee.added"

    @Column(columnDefinition = "text")
    private String data; // JSON blob

    private boolean read;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_employee_id")
    private BusinessEmployee businessEmployee;

    public Notification() {}

    public Notification(Long id, String type, String data, boolean read, Instant createdAt, BusinessEmployee businessEmployee) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.read = read;
        this.createdAt = createdAt;
        this.businessEmployee = businessEmployee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public BusinessEmployee getBusinessEmployee() {
        return businessEmployee;
    }

    public void setBusinessEmployee(BusinessEmployee businessEmployee) {
        this.businessEmployee = businessEmployee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return (
            read == that.read &&
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(data, that.data) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(businessEmployee, that.businessEmployee)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, data, read, createdAt, businessEmployee);
    }

    @Override
    public String toString() {
        return (
            "Notification{" +
            "id=" +
            id +
            ", type='" +
            type +
            '\'' +
            ", data='" +
            data +
            '\'' +
            ", read=" +
            read +
            ", createdAt=" +
            createdAt +
            ", businessEmployee=" +
            businessEmployee +
            '}'
        );
    }
}
