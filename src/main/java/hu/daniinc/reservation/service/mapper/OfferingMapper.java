package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.service.dto.AppointmentDTO;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Offering} and its DTO {@link OfferingDTO}.
 */
@Mapper(componentModel = "spring")
public interface OfferingMapper extends EntityMapper<OfferingDTO, Offering> {
    @Mapping(target = "businessEmployee", source = "businessEmployee", ignore = true)
    OfferingDTO toDto(Offering s);
}
