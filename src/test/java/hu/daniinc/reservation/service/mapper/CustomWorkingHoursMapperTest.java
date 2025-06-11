package hu.daniinc.reservation.service.mapper;

import static hu.daniinc.reservation.domain.CustomWorkingHoursAsserts.*;
import static hu.daniinc.reservation.domain.CustomWorkingHoursTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomWorkingHoursMapperTest {

    private CustomWorkingHoursMapper customWorkingHoursMapper;

    @BeforeEach
    void setUp() {
        customWorkingHoursMapper = new CustomWorkingHoursMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCustomWorkingHoursSample1();
        var actual = customWorkingHoursMapper.toEntity(customWorkingHoursMapper.toDto(expected));
        assertCustomWorkingHoursAllPropertiesEquals(expected, actual);
    }
}
