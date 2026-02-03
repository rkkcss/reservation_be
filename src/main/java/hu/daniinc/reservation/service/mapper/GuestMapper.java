package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.service.dto.GuestDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Guest} and its DTO {@link GuestDTO}.
 */
@Mapper(componentModel = "spring", uses = { BusinessEmployeeMapper.class })
public interface GuestMapper extends EntityMapper<GuestDTO, Guest> {
    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "businessEmployee", source = "businessEmployee")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "canBook", source = "canBook")
    GuestDTO toDto(Guest guest);
}
