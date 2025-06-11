package hu.daniinc.reservation.service.mapper;

import static hu.daniinc.reservation.domain.BusinessOpeningHoursAsserts.*;
import static hu.daniinc.reservation.domain.BusinessOpeningHoursTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BusinessOpeningHoursMapperTest {

    private BusinessOpeningHoursMapper businessOpeningHoursMapper;

    @BeforeEach
    void setUp() {
        businessOpeningHoursMapper = new BusinessOpeningHoursMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBusinessOpeningHoursSample1();
        var actual = businessOpeningHoursMapper.toEntity(businessOpeningHoursMapper.toDto(expected));
        assertBusinessOpeningHoursAllPropertiesEquals(expected, actual);
    }
}
