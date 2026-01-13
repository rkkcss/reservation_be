package hu.daniinc.reservation.service.dto;

public class RevenueProjection {

    private Long revenue;
    private Long count;

    public RevenueProjection(Object revenue, Object count) {
        this.revenue = revenue != null ? ((Number) revenue).longValue() : 0L;
        this.count = count != null ? ((Number) count).longValue() : 0L;
    }

    public Long getRevenue() {
        return revenue;
    }

    public Long getCount() {
        return count;
    }
}
