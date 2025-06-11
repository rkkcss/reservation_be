package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.BusinessTestSamples.*;
import static hu.daniinc.reservation.domain.WorkingHoursTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WorkingHoursTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WorkingHours.class);
        WorkingHours workingHours1 = getWorkingHoursSample1();
        WorkingHours workingHours2 = new WorkingHours();
        assertThat(workingHours1).isNotEqualTo(workingHours2);

        workingHours2.setId(workingHours1.getId());
        assertThat(workingHours1).isEqualTo(workingHours2);

        workingHours2 = getWorkingHoursSample2();
        assertThat(workingHours1).isNotEqualTo(workingHours2);
    }

    @Test
    void businessTest() {
        WorkingHours workingHours = getWorkingHoursRandomSampleGenerator();
        Business businessBack = getBusinessRandomSampleGenerator();

        workingHours.setBusiness(businessBack);
        assertThat(workingHours.getBusiness()).isEqualTo(businessBack);

        workingHours.business(null);
        assertThat(workingHours.getBusiness()).isNull();
    }
}
