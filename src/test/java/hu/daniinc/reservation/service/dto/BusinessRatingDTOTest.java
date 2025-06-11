package hu.daniinc.reservation.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BusinessRatingDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BusinessRatingDTO.class);
        BusinessRatingDTO businessRatingDTO1 = new BusinessRatingDTO();
        businessRatingDTO1.setId(1L);
        BusinessRatingDTO businessRatingDTO2 = new BusinessRatingDTO();
        assertThat(businessRatingDTO1).isNotEqualTo(businessRatingDTO2);
        businessRatingDTO2.setId(businessRatingDTO1.getId());
        assertThat(businessRatingDTO1).isEqualTo(businessRatingDTO2);
        businessRatingDTO2.setId(2L);
        assertThat(businessRatingDTO1).isNotEqualTo(businessRatingDTO2);
        businessRatingDTO1.setId(null);
        assertThat(businessRatingDTO1).isNotEqualTo(businessRatingDTO2);
    }
}
