package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.service.dto.AppointmentDTO;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.GuestDTO;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper extends EntityMapper<AppointmentDTO, Appointment> {
    @Mapping(target = "guest", source = "guest", qualifiedByName = "guestId")
    @Mapping(target = "offering", source = "offering", qualifiedByName = "offeringMap")
    AppointmentDTO toDto(Appointment s);

    @Named("guestId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    GuestDTO toDtoGuestId(Guest guest);

    @Named("offeringMap")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "durationMinutes", source = "durationMinutes")
    OfferingDTO toDtoOffering(Offering offering);
}
