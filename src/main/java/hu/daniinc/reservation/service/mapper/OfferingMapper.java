package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Offering} and its DTO {@link OfferingDTO}.
 */
@Mapper(componentModel = "spring")
public interface OfferingMapper extends EntityMapper<OfferingDTO, Offering> {
    @Mapping(target = "businessEmployee", source = "businessEmployee", qualifiedByName = "toDtoBusinessEmployee")
    OfferingDTO toDto(Offering s);

    @Named("toDtoBusinessEmployee")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "appointments", source = "appointments", ignore = true)
    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "user.firstName", source = "user.firstName")
    @Mapping(target = "user.lastName", source = "user.lastName")
    @Mapping(target = "user.fullName", source = "user.fullName")
    BusinessEmployeeDTO businessEmployeeToDto(BusinessEmployee businessEmployee);
}
