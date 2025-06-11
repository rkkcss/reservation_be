package hu.daniinc.reservation.service.mapper;

import static hu.daniinc.reservation.domain.WorkingHoursAsserts.*;
import static hu.daniinc.reservation.domain.WorkingHoursTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkingHoursMapperTest {

    private WorkingHoursMapper workingHoursMapper;

    @BeforeEach
    void setUp() {
        workingHoursMapper = new WorkingHoursMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getWorkingHoursSample1();
        var actual = workingHoursMapper.toEntity(workingHoursMapper.toDto(expected));
        assertWorkingHoursAllPropertiesEquals(expected, actual);
    }
}
