package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.BusinessRatingTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BusinessRatingTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BusinessRating.class);
        BusinessRating businessRating1 = getBusinessRatingSample1();
        BusinessRating businessRating2 = new BusinessRating();
        assertThat(businessRating1).isNotEqualTo(businessRating2);

        businessRating2.setId(businessRating1.getId());
        assertThat(businessRating1).isEqualTo(businessRating2);

        businessRating2 = getBusinessRatingSample2();
        assertThat(businessRating1).isNotEqualTo(businessRating2);
    }
}
