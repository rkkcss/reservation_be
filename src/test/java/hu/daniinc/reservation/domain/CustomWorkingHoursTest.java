package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.BusinessTestSamples.*;
import static hu.daniinc.reservation.domain.CustomWorkingHoursTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CustomWorkingHoursTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CustomWorkingHours.class);
        CustomWorkingHours customWorkingHours1 = getCustomWorkingHoursSample1();
        CustomWorkingHours customWorkingHours2 = new CustomWorkingHours();
        assertThat(customWorkingHours1).isNotEqualTo(customWorkingHours2);

        customWorkingHours2.setId(customWorkingHours1.getId());
        assertThat(customWorkingHours1).isEqualTo(customWorkingHours2);

        customWorkingHours2 = getCustomWorkingHoursSample2();
        assertThat(customWorkingHours1).isNotEqualTo(customWorkingHours2);
    }

    @Test
    void businessTest() {
        CustomWorkingHours customWorkingHours = getCustomWorkingHoursRandomSampleGenerator();
        Business businessBack = getBusinessRandomSampleGenerator();

        customWorkingHours.setBusiness(businessBack);
        assertThat(customWorkingHours.getBusiness()).isEqualTo(businessBack);

        customWorkingHours.business(null);
        assertThat(customWorkingHours.getBusiness()).isNull();
    }
}
