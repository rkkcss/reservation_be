package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.BusinessOpeningHours;
import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BusinessOpeningHours} and its DTO {@link BusinessOpeningHoursDTO}.
 */
@Mapper(componentModel = "spring")
public interface BusinessOpeningHoursMapper extends EntityMapper<BusinessOpeningHoursDTO, BusinessOpeningHours> {}
