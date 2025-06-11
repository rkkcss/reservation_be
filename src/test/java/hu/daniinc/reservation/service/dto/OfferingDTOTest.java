package hu.daniinc.reservation.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OfferingDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(OfferingDTO.class);
        OfferingDTO offeringDTO1 = new OfferingDTO();
        offeringDTO1.setId(1L);
        OfferingDTO offeringDTO2 = new OfferingDTO();
        assertThat(offeringDTO1).isNotEqualTo(offeringDTO2);
        offeringDTO2.setId(offeringDTO1.getId());
        assertThat(offeringDTO1).isEqualTo(offeringDTO2);
        offeringDTO2.setId(2L);
        assertThat(offeringDTO1).isNotEqualTo(offeringDTO2);
        offeringDTO1.setId(null);
        assertThat(offeringDTO1).isNotEqualTo(offeringDTO2);
    }
}
