package hu.daniinc.reservation.service.dto;

public class BusinessEmployeeActivateDTO {

    private BusinessEmployeeInviteDTO businessEmployeeInvite;
    private boolean userExists;

    public BusinessEmployeeInviteDTO getBusinessEmployeeInvite() {
        return businessEmployeeInvite;
    }

    public void setBusinessEmployeeInvite(BusinessEmployeeInviteDTO businessEmployeeInvite) {
        this.businessEmployeeInvite = businessEmployeeInvite;
    }

    public boolean isUserExists() {
        return userExists;
    }

    public void setUserExists(boolean userExists) {
        this.userExists = userExists;
    }
}
