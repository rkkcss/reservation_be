package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.AppointmentTestSamples.*;
import static hu.daniinc.reservation.domain.GuestTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GuestTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Guest.class);
        Guest guest1 = getGuestSample1();
        Guest guest2 = new Guest();
        assertThat(guest1).isNotEqualTo(guest2);

        guest2.setId(guest1.getId());
        assertThat(guest1).isEqualTo(guest2);

        guest2 = getGuestSample2();
        assertThat(guest1).isNotEqualTo(guest2);
    }

    @Test
    void appointmentTest() {}
}
