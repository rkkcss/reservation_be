package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.BusinessRating;
import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BusinessRating} and its DTO {@link BusinessRatingDTO}.
 */
@Mapper(componentModel = "spring")
public interface BusinessRatingMapper extends EntityMapper<BusinessRatingDTO, BusinessRating> {}
