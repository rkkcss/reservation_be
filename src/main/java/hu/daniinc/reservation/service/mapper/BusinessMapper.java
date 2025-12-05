package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Business} and its DTO {@link BusinessDTO}.
 */
@Mapper(componentModel = "spring")
public interface BusinessMapper extends EntityMapper<BusinessDTO, Business> {
    @Named("businessToDTO")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "ownerId")
    @Mapping(target = "workingHours", source = "workingHours", ignore = true)
    @Mapping(target = "theme", source = "theme")
    BusinessDTO toDto(Business s);

    @Named("ownerId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
