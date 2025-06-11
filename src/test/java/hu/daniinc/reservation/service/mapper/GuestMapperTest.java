package hu.daniinc.reservation.service.mapper;

import static hu.daniinc.reservation.domain.GuestAsserts.*;
import static hu.daniinc.reservation.domain.GuestTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuestMapperTest {

    private GuestMapper guestMapper;

    @BeforeEach
    void setUp() {
        guestMapper = new GuestMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getGuestSample1();
        var actual = guestMapper.toEntity(guestMapper.toDto(expected));
        assertGuestAllPropertiesEquals(expected, actual);
    }
}
