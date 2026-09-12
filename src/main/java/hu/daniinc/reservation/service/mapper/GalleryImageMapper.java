package hu.daniinc.reservation.service.mapper;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.GalleryImage;
import hu.daniinc.reservation.service.dto.BusinessEmployeeDTO;
import hu.daniinc.reservation.service.dto.GalleryImageDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { GalleryImageMapper.class })
public interface GalleryImageMapper extends EntityMapper<GalleryImageDTO, GalleryImage> {
    GalleryImageDTO toDto(GalleryImage galleryImage);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "role", source = "role")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "modifiedDate", source = "modifiedDate")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "business", source = "business")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "permissions", source = "permissions")
    BusinessEmployeeDTO businessEmployeeToBusinessEmployeeDTO(BusinessEmployee businessEmployee);
}
