package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.EmployeeTimeOff;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.service.dto.CreateTimeOffDTO;
import hu.daniinc.reservation.service.dto.EmployeeTimeOffDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface EmployeeTimeOffMapper extends EntityMapper<EmployeeTimeOffDTO, EmployeeTimeOff> {
    @Mapping(target = "businessEmployee", source = "businessEmployee")
    EmployeeTimeOffDTO toDto(EmployeeTimeOff entity);

    @Mapping(target = "businessEmployee", source = "businessEmployee")
    @Mapping(target = "startInstant", ignore = true)
    @Mapping(target = "endInstant", ignore = true)
    EmployeeTimeOff toEntity(EmployeeTimeOffDTO dto);

    // CreateTimeOffDTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessEmployee", source = "businessEmployeeId", qualifiedByName = "businessEmployeeFromId")
    @Mapping(target = "startInstant", ignore = true)
    @Mapping(target = "endInstant", ignore = true)
    @Mapping(target = "status", ignore = true)
    EmployeeTimeOff toEntity(CreateTimeOffDTO dto);

    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "guests", ignore = true)
    BusinessEmployeeDTO businessEmployeeToBusinessEmployeeDTO(BusinessEmployee entity);

    @Named("businessEmployeeFromId")
    default BusinessEmployee businessEmployeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        BusinessEmployee businessEmployee = new BusinessEmployee();
        businessEmployee.setId(id);
        return businessEmployee;
    }
}
