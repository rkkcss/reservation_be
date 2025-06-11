package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.WorkingHours;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.WorkingHoursDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link WorkingHours} and its DTO {@link WorkingHoursDTO}.
 */
@Mapper(componentModel = "spring")
public interface WorkingHoursMapper extends EntityMapper<WorkingHoursDTO, WorkingHours> {
    WorkingHoursDTO toDto(WorkingHours s);
}
