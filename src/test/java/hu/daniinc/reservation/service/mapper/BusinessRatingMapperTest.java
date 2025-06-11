package hu.daniinc.reservation.service.mapper;

import static hu.daniinc.reservation.domain.BusinessRatingAsserts.*;
import static hu.daniinc.reservation.domain.BusinessRatingTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BusinessRatingMapperTest {

    private BusinessRatingMapper businessRatingMapper;

    @BeforeEach
    void setUp() {
        businessRatingMapper = new BusinessRatingMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBusinessRatingSample1();
        var actual = businessRatingMapper.toEntity(businessRatingMapper.toDto(expected));
        assertBusinessRatingAllPropertiesEquals(expected, actual);
    }
}
