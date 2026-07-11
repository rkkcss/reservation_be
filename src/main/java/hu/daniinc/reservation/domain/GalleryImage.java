package hu.daniinc.reservation.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String publicId;
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_employee_id", nullable = false)
    private BusinessEmployee businessEmployee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
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
}
