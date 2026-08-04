package hu.daniinc.reservation.service.dto;

import java.util.Objects;

public class OnboardingCompleteDTO {

    private BusinessDTO business;
    private WorkingHoursDTO workingHours;

    public BusinessDTO getBusiness() {
        return business;
    }

    public void setBusiness(BusinessDTO business) {
        this.business = business;
    }

    public WorkingHoursDTO getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(WorkingHoursDTO workingHours) {
        this.workingHours = workingHours;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OnboardingCompleteDTO that = (OnboardingCompleteDTO) o;
        return Objects.equals(business, that.business) && Objects.equals(workingHours, that.workingHours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(business, workingHours);
    }
}
