package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.service.dto.AdminUserDTO;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.service.dto.UserDTO;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    uses = { BusinessMapper.class },
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BusinessEmployeeMapper extends EntityMapper<BusinessEmployeeDTO, BusinessEmployee> {
    @Mapping(target = "business", source = "business", qualifiedByName = "businessToDTO")
    @Mapping(target = "user", source = "user", qualifiedByName = "userToAdminUserDTOMapper")
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "workingHours", source = "workingHours", ignore = true)
    BusinessEmployeeDTO toDto(BusinessEmployee businessEmployee);

    //    @Named("businessToDto")
    BusinessDTO businessDtoMapper(Business b);

    @Named("userToAdminUserDTOMapper")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    UserDTO userToAdminUserDTOMapper(User user);
}
