package hu.daniinc.reservation.service.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public class BusinessRatingSummaryDTO {

    private List<BusinessRatingDTO> ratings;
    private Double averageRating;

    public BusinessRatingSummaryDTO() {}

    public BusinessRatingSummaryDTO(List<BusinessRatingDTO> ratings, Double averageRating) {
        this.ratings = ratings;
        this.averageRating = averageRating;
    }

    public List<BusinessRatingDTO> getRatings() {
        return ratings;
    }

    public void setRatings(List<BusinessRatingDTO> ratings) {
        this.ratings = ratings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
