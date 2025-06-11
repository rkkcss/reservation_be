package hu.daniinc.reservation.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CustomWorkingHoursDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CustomWorkingHoursDTO.class);
        CustomWorkingHoursDTO customWorkingHoursDTO1 = new CustomWorkingHoursDTO();
        customWorkingHoursDTO1.setId(1L);
        CustomWorkingHoursDTO customWorkingHoursDTO2 = new CustomWorkingHoursDTO();
        assertThat(customWorkingHoursDTO1).isNotEqualTo(customWorkingHoursDTO2);
        customWorkingHoursDTO2.setId(customWorkingHoursDTO1.getId());
        assertThat(customWorkingHoursDTO1).isEqualTo(customWorkingHoursDTO2);
        customWorkingHoursDTO2.setId(2L);
        assertThat(customWorkingHoursDTO1).isNotEqualTo(customWorkingHoursDTO2);
        customWorkingHoursDTO1.setId(null);
        assertThat(customWorkingHoursDTO1).isNotEqualTo(customWorkingHoursDTO2);
    }
}
