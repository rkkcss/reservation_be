package hu.daniinc.reservation.domain.enumeration;

import java.util.EnumSet;
import java.util.Set;

public enum BusinessPermissionHandler {
    OWNER(
        Set.of(
            BusinessPermission.VIEW_OWN_SCHEDULE,
            BusinessPermission.VIEW_ALL_SCHEDULE,
            BusinessPermission.EDIT_OWN_SCHEDULE,
            BusinessPermission.EDIT_ALL_SCHEDULES,
            BusinessPermission.CREATE_BOOKING,
            BusinessPermission.EDIT_OWN_BOOKINGS,
            BusinessPermission.EDIT_ALL_BOOKINGS,
            BusinessPermission.VIEW_SERVICES,
            BusinessPermission.EDIT_OWN_SERVICES,
            BusinessPermission.EDIT_ALL_SERVICES,
            BusinessPermission.VIEW_OWN_STATISTICS,
            BusinessPermission.VIEW_ALL_STATISTICS,
            BusinessPermission.MANAGE_EMPLOYEES,
            BusinessPermission.VIEW_EMPLOYEES,
            BusinessPermission.MANAGE_BUSINESS_SETTINGS,
            BusinessPermission.EDIT_OWN_WORKING_HOURS,
            BusinessPermission.EDIT_ALL_WORKING_HOURS
        )
    );

    private final Set<BusinessPermission> permissions;

    BusinessPermissionHandler(Set<BusinessPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<BusinessPermission> getPermissions() {
        return EnumSet.copyOf(permissions);
    }
}
