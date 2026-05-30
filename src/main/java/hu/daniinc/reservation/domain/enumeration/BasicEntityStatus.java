package hu.daniinc.reservation.domain.enumeration;

public enum BasicEntityStatus {
    ACTIVE,
    INACTIVE, // can see on UI
    DELETED, //soft delete -> can't see on UI
}
