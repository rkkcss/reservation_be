package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployeeInvite;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.BusinessEmployeeInviteDTO;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    uses = { BusinessEmployeeInvite.class, BusinessMapper.class },
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BusinessEmployeeInviteMapper extends EntityMapper<BusinessEmployeeInviteDTO, BusinessEmployeeInvite> {
    @Named("withToken")
    BusinessEmployeeInviteDTO toDto(BusinessEmployeeInvite businessEmployeeInvite);

    @Named("withoutToken")
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "business", source = "business", qualifiedByName = "businessToDTO")
    BusinessEmployeeInviteDTO toDtoWithoutToken(BusinessEmployeeInvite businessEmployeeInvite);
}
