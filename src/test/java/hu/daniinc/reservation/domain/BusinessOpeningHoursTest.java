package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.BusinessOpeningHoursTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BusinessOpeningHoursTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BusinessOpeningHours.class);
        BusinessOpeningHours businessOpeningHours1 = getBusinessOpeningHoursSample1();
        BusinessOpeningHours businessOpeningHours2 = new BusinessOpeningHours();
        assertThat(businessOpeningHours1).isNotEqualTo(businessOpeningHours2);

        businessOpeningHours2.setId(businessOpeningHours1.getId());
        assertThat(businessOpeningHours1).isEqualTo(businessOpeningHours2);

        businessOpeningHours2 = getBusinessOpeningHoursSample2();
        assertThat(businessOpeningHours1).isNotEqualTo(businessOpeningHours2);
    }
}
