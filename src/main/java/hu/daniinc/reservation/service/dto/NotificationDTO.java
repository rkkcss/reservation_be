package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.User;
import java.time.Instant;
import java.util.Map;

public class NotificationDTO {

    private Long id;

    private String type; // "appointment.cancelled", "employee.added"

    private Map<String, Object> data; // JSON blob

    private boolean read;

    private Instant createdAt;

    public NotificationDTO() {}

    public NotificationDTO(Long id, String type, Map<String, Object> data, boolean read, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.read = read;
        this.createdAt = createdAt;
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

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
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
}
