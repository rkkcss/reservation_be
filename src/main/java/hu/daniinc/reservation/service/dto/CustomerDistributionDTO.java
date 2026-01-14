package hu.daniinc.reservation.service.dto;

public class CustomerDistributionDTO {

    private Double returningPercentage;
    private Double newPercentage;
    private String topCustomerName;
    private Long topCustomerBookings;
    private Long returningGuestCount;
    private Long newGuestCount;

    public CustomerDistributionDTO(
        Double returningPercentage,
        Double newPercentage,
        String topCustomerName,
        Long topCustomerBookings,
        Long returningGuestCount,
        Long newGuestCount
    ) {
        this.returningPercentage = returningPercentage != null ? returningPercentage : 0.0;
        this.newPercentage = newPercentage != null ? newPercentage : 0.0;
        this.topCustomerName = topCustomerName != null ? topCustomerName : "no-data";
        this.topCustomerBookings = topCustomerBookings != null ? topCustomerBookings : 0L;
        this.returningGuestCount = returningGuestCount != null ? returningGuestCount : 0L;
        this.newGuestCount = newGuestCount != null ? newGuestCount : 0L;
    }

    public CustomerDistributionDTO(Double returningPercentage, Double newPercentage, String topCustomerName, Long topCustomerBookings) {
        this.returningPercentage = returningPercentage;
        this.newPercentage = newPercentage;
        this.topCustomerName = topCustomerName;
        this.topCustomerBookings = topCustomerBookings;
    }

    public Long getReturningGuestCount() {
        return returningGuestCount;
    }

    public void setReturningGuestCount(Long returningGuestCount) {
        this.returningGuestCount = returningGuestCount;
    }

    public Long getNewGuestCount() {
        return newGuestCount;
    }

    public void setNewGuestCount(Long newGuestCount) {
        this.newGuestCount = newGuestCount;
    }

    public CustomerDistributionDTO() {}

    public Double getReturningPercentage() {
        return returningPercentage;
    }

    public void setReturningPercentage(Double returningPercentage) {
        this.returningPercentage = returningPercentage;
    }

    public Double getNewPercentage() {
        return newPercentage;
    }

    public void setNewPercentage(Double newPercentage) {
        this.newPercentage = newPercentage;
    }

    public String getTopCustomerName() {
        return topCustomerName;
    }

    public void setTopCustomerName(String topCustomerName) {
        this.topCustomerName = topCustomerName;
    }

    public Long getTopCustomerBookings() {
        return topCustomerBookings;
    }

    public void setTopCustomerBookings(Long topCustomerBookings) {
        this.topCustomerBookings = topCustomerBookings;
    }
}
