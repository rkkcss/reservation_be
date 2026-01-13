package hu.daniinc.reservation.service.dto;

public class BusinessStatisticSummaryDTO {

    private Long totalRevenue;
    private Long totalAppointments;
    private Long newCustomers;
    private Double averageRating;
    private Long reviewCount;

    public BusinessStatisticSummaryDTO() {}

    public BusinessStatisticSummaryDTO(
        Long totalRevenue,
        Long totalAppointments,
        Long newCustomers,
        Double averageRating,
        Long reviewCount
    ) {
        this.totalRevenue = totalRevenue;
        this.totalAppointments = totalAppointments;
        this.newCustomers = newCustomers;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(Long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public Long getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(Long newCustomers) {
        this.newCustomers = newCustomers;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Long getTotalRevenue() {
        return totalRevenue;
    }
}
