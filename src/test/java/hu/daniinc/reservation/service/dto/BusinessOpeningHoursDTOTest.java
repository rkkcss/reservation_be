package hu.daniinc.reservation.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BusinessOpeningHoursDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BusinessOpeningHoursDTO.class);
        BusinessOpeningHoursDTO businessOpeningHoursDTO1 = new BusinessOpeningHoursDTO();
        businessOpeningHoursDTO1.setId(1L);
        BusinessOpeningHoursDTO businessOpeningHoursDTO2 = new BusinessOpeningHoursDTO();
        assertThat(businessOpeningHoursDTO1).isNotEqualTo(businessOpeningHoursDTO2);
        businessOpeningHoursDTO2.setId(businessOpeningHoursDTO1.getId());
        assertThat(businessOpeningHoursDTO1).isEqualTo(businessOpeningHoursDTO2);
        businessOpeningHoursDTO2.setId(2L);
        assertThat(businessOpeningHoursDTO1).isNotEqualTo(businessOpeningHoursDTO2);
        businessOpeningHoursDTO1.setId(null);
        assertThat(businessOpeningHoursDTO1).isNotEqualTo(businessOpeningHoursDTO2);
    }
}
