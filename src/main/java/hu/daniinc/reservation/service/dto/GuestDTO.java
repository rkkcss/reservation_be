package hu.daniinc.reservation.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link hu.daniinc.reservation.domain.Guest} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GuestDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String email;

    private String phoneNumber;

    private Boolean canBook = true;

    private Instant createdDate;

    private BusinessEmployeeDTO businessEmployee;

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getCanBook() {
        return canBook;
    }

    public void setCanBook(Boolean canBook) {
        this.canBook = canBook;
    }

    private BusinessEmployeeDTO businessEmployeeDTO;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GuestDTO)) {
            return false;
        }

        GuestDTO guestDTO = (GuestDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, guestDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GuestDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", email='" + getEmail() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            "}";
    }
}
