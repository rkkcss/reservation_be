package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.*;
import hu.daniinc.reservation.service.dto.*;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper extends EntityMapper<AppointmentDTO, Appointment> {
    @Mapping(target = "guest", source = "guest", qualifiedByName = "guestId")
    //    @Mapping(target = "guest.appointments", ignore = true)
    @Mapping(target = "offering", source = "offering", qualifiedByName = "offeringMap")
    @Mapping(target = "businessEmployee", source = "businessEmployee", qualifiedByName = "employeeSimple")
    AppointmentDTO toDto(Appointment s);

    @Named("guestId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    GuestDTO toDtoGuestId(Guest guest);

    @Named("offeringMap")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "durationMinutes", source = "durationMinutes")
    OfferingDTO toDtoOffering(Offering offering);

    @Named("employeeSimple")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "user.firstName", source = "user.firstName")
    @Mapping(target = "user.lastName", source = "user.lastName")
    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "user.fullName", source = "user.fullName")
    @Mapping(target = "user.email", source = "user.email")
    BusinessEmployeeDTO toDtoEmployeeSimple(BusinessEmployee employee);
}
