package hu.daniinc.reservation.domain;

import static hu.daniinc.reservation.domain.AppointmentTestSamples.*;
import static hu.daniinc.reservation.domain.BusinessTestSamples.*;
import static hu.daniinc.reservation.domain.CustomWorkingHoursTestSamples.*;
import static hu.daniinc.reservation.domain.OfferingTestSamples.*;
import static hu.daniinc.reservation.domain.WorkingHoursTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import hu.daniinc.reservation.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BusinessTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Business.class);
        Business business1 = getBusinessSample1();
        Business business2 = new Business();
        assertThat(business1).isNotEqualTo(business2);

        business2.setId(business1.getId());
        assertThat(business1).isEqualTo(business2);

        business2 = getBusinessSample2();
        assertThat(business1).isNotEqualTo(business2);
    }

    @Test
    void appointmentTest() {}

    @Test
    void workingHoursTest() {
        Business business = getBusinessRandomSampleGenerator();
        WorkingHours workingHoursBack = getWorkingHoursRandomSampleGenerator();

        business.addWorkingHours(workingHoursBack);
        assertThat(business.getWorkingHours()).containsOnly(workingHoursBack);
        assertThat(workingHoursBack.getBusiness()).isEqualTo(business);

        business.removeWorkingHours(workingHoursBack);
        assertThat(business.getWorkingHours()).doesNotContain(workingHoursBack);
        assertThat(workingHoursBack.getBusiness()).isNull();

        business.workingHours(new HashSet<>(Set.of(workingHoursBack)));
        assertThat(business.getWorkingHours()).containsOnly(workingHoursBack);
        assertThat(workingHoursBack.getBusiness()).isEqualTo(business);

        business.setWorkingHours(new HashSet<>());
        assertThat(business.getWorkingHours()).doesNotContain(workingHoursBack);
        assertThat(workingHoursBack.getBusiness()).isNull();
    }

    @Test
    void customWorkingHoursTest() {
        Business business = getBusinessRandomSampleGenerator();
        CustomWorkingHours customWorkingHoursBack = getCustomWorkingHoursRandomSampleGenerator();

        business.addCustomWorkingHours(customWorkingHoursBack);
        assertThat(business.getCustomWorkingHours()).containsOnly(customWorkingHoursBack);
        assertThat(customWorkingHoursBack.getBusiness()).isEqualTo(business);

        business.removeCustomWorkingHours(customWorkingHoursBack);
        assertThat(business.getCustomWorkingHours()).doesNotContain(customWorkingHoursBack);
        assertThat(customWorkingHoursBack.getBusiness()).isNull();

        business.customWorkingHours(new HashSet<>(Set.of(customWorkingHoursBack)));
        assertThat(business.getCustomWorkingHours()).containsOnly(customWorkingHoursBack);
        assertThat(customWorkingHoursBack.getBusiness()).isEqualTo(business);

        business.setCustomWorkingHours(new HashSet<>());
        assertThat(business.getCustomWorkingHours()).doesNotContain(customWorkingHoursBack);
        assertThat(customWorkingHoursBack.getBusiness()).isNull();
    }

    @Test
    void offeringTest() {
        Business business = getBusinessRandomSampleGenerator();
        Offering offeringBack = getOfferingRandomSampleGenerator();

        business.addOffering(offeringBack);
        assertThat(business.getOfferings()).containsOnly(offeringBack);
        assertThat(offeringBack.getBusiness()).isEqualTo(business);

        business.removeOffering(offeringBack);
        assertThat(business.getOfferings()).doesNotContain(offeringBack);
        assertThat(offeringBack.getBusiness()).isNull();

        business.offerings(new HashSet<>(Set.of(offeringBack)));
        assertThat(business.getOfferings()).containsOnly(offeringBack);
        assertThat(offeringBack.getBusiness()).isEqualTo(business);

        business.setOfferings(new HashSet<>());
        assertThat(business.getOfferings()).doesNotContain(offeringBack);
        assertThat(offeringBack.getBusiness()).isNull();
    }
}
