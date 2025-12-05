package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.AppointmentTestSamples.*;
import static hu.daniinc.reservation.domain.BusinessTestSamples.*;
import static hu.daniinc.reservation.domain.GuestTestSamples.*;
import static hu.daniinc.reservation.domain.OfferingTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Appointment.class);
        Appointment appointment1 = getAppointmentSample1();
        Appointment appointment2 = new Appointment();
        assertThat(appointment1).isNotEqualTo(appointment2);

        appointment2.setId(appointment1.getId());
        assertThat(appointment1).isEqualTo(appointment2);

        appointment2 = getAppointmentSample2();
        assertThat(appointment1).isNotEqualTo(appointment2);
    }

    @Test
    void guestTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Guest guestBack = getGuestRandomSampleGenerator();

        appointment.setGuest(guestBack);
        assertThat(appointment.getGuest()).isEqualTo(guestBack);

        appointment.guest(null);
        assertThat(appointment.getGuest()).isNull();
    }

    @Test
    void businessTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Business businessBack = getBusinessRandomSampleGenerator();
        //        appointment.setBusiness(businessBack);
        //        assertThat(appointment.getBusiness()).isEqualTo(businessBack);

        //        appointment.business(null);
        //        assertThat(appointment.getBusiness()).isNull();
    }

    @Test
    void offeringTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Offering offeringBack = getOfferingRandomSampleGenerator();
    }
}
