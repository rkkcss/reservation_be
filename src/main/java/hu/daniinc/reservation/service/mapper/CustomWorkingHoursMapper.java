package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.CustomWorkingHours;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.CustomWorkingHoursDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CustomWorkingHours} and its DTO {@link CustomWorkingHoursDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomWorkingHoursMapper extends EntityMapper<CustomWorkingHoursDTO, CustomWorkingHours> {
    @Mapping(target = "business", source = "business", qualifiedByName = "businessId")
    CustomWorkingHoursDTO toDto(CustomWorkingHours s);

    @Named("businessId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BusinessDTO toDtoBusinessId(Business business);
}
