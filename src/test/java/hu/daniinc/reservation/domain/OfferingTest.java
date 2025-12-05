package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.AppointmentTestSamples.*;
import static hu.daniinc.reservation.domain.BusinessTestSamples.*;
import static hu.daniinc.reservation.domain.OfferingTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OfferingTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Offering.class);
        Offering offering1 = getOfferingSample1();
        Offering offering2 = new Offering();
        assertThat(offering1).isNotEqualTo(offering2);

        offering2.setId(offering1.getId());
        assertThat(offering1).isEqualTo(offering2);

        offering2 = getOfferingSample2();
        assertThat(offering1).isNotEqualTo(offering2);
    }

    @Test
    void businessTest() {
        Offering offering = getOfferingRandomSampleGenerator();
        Business businessBack = getBusinessRandomSampleGenerator();
    }

    @Test
    void appointmentTest() {
        Offering offering = getOfferingRandomSampleGenerator();
        Appointment appointmentBack = getAppointmentRandomSampleGenerator();
    }
}
