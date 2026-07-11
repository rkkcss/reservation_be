package hu.daniinc.reservation.service.dto;

import java.time.Instant;

public class GalleryImageDTO {

    private Long id;

    private String url;
    private String publicId;
    private Instant createdAt;

    private BusinessEmployeeDTO businessEmployee;

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

    public BusinessEmployeeDTO getBusinessEmployee() {
        return businessEmployee;
    }

    public void setBusinessEmployee(BusinessEmployeeDTO businessEmployee) {
        this.businessEmployee = businessEmployee;
    }
}
