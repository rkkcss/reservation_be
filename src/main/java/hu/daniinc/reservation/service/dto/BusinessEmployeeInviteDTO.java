package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.domain.enumeration.BusinessRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class BusinessEmployeeInviteDTO {

    private Long id;

    private String email;

    private Business business;

    private BusinessRole role;

    private Set<BusinessPermission> permissions = new HashSet<>();

    private String token;
    private Instant expiresAt;
    private boolean used;

    private User invitedBy;

    //    getter + setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }
}
