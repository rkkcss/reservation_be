package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.GalleryImage;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.service.dto.GalleryImageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { GalleryImageMapper.class })
public interface GalleryImageMapper extends EntityMapper<GalleryImageDTO, GalleryImage> {
    GalleryImageDTO toDto(GalleryImage galleryImage);

    @Mapping(target = "appointments", ignore = true)
    BusinessEmployeeDTO businessEmployeeToBusinessEmployeeDTO(BusinessEmployee businessEmployee);
}
