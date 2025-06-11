package hu.daniinc.reservation.service.mapper;

import static hu.daniinc.reservation.domain.OfferingAsserts.*;
import static hu.daniinc.reservation.domain.OfferingTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfferingMapperTest {

    private OfferingMapper offeringMapper;

    @BeforeEach
    void setUp() {
        offeringMapper = new OfferingMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getOfferingSample1();
        var actual = offeringMapper.toEntity(offeringMapper.toDto(expected));
        assertOfferingAllPropertiesEquals(expected, actual);
    }
}
