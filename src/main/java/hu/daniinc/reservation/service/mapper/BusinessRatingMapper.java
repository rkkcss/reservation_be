package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessRating;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
import hu.daniinc.reservation.service.dto.GuestDTO;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BusinessRating} and its DTO {@link BusinessRatingDTO}.
 */
@Mapper(componentModel = "spring")
public interface BusinessRatingMapper extends EntityMapper<BusinessRatingDTO, BusinessRating> {
    @Mapping(target = "guest", source = "guest", qualifiedByName = "guest")
    BusinessRatingDTO toDto(BusinessRating s);

    @Named("guest")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    GuestDTO toDtoGuest(Guest guest);
}
