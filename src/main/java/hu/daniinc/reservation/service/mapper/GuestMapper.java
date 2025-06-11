package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.service.dto.GuestDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Guest} and its DTO {@link GuestDTO}.
 */
@Mapper(componentModel = "spring")
public interface GuestMapper extends EntityMapper<GuestDTO, Guest> {}
