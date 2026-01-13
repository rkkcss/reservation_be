package hu.daniinc.reservation.service.dto;

public class RatingProjection {

    private Double average;
    private Long count;

    public RatingProjection() {}

    public RatingProjection(Object average, Object count) {
        // Így akkor is működik, ha az AVG eredménye Integer vagy Float
        this.average = average != null ? ((Number) average).doubleValue() : 0.0;
        this.count = count != null ? ((Number) count).longValue() : 0L;
    }

    public Double getAverage() {
        return average;
    }

    public Long getCount() {
        return count;
    }
}
