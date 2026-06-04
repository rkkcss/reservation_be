package hu.daniinc.reservation.service.dto;

import java.util.List;

public class GlobalSearchResponse {

    private List<AppointmentDTO> appointments;
    private List<GuestDTO> guests;
    private List<BusinessEmployeeDTO> employees;
    private long totalCount;

    public GlobalSearchResponse(
        List<AppointmentDTO> appointments,
        List<GuestDTO> guests,
        List<BusinessEmployeeDTO> employees,
        long totalCount
    ) {
        this.appointments = appointments;
        this.guests = guests;
        this.employees = employees;
        this.totalCount = totalCount;
    }

    public GlobalSearchResponse(List<GuestDTO> guests, List<AppointmentDTO> appointments, long totalCount) {
        this.appointments = appointments;
        this.guests = guests;
        this.totalCount = totalCount;
    }

    public List<AppointmentDTO> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentDTO> appointments) {
        this.appointments = appointments;
    }

    public List<GuestDTO> getGuests() {
        return guests;
    }

    public void setGuests(List<GuestDTO> guests) {
        this.guests = guests;
    }

    public List<BusinessEmployeeDTO> getEmployees() {
        return employees;
    }

    public void setEmployees(List<BusinessEmployeeDTO> employees) {
        this.employees = employees;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
