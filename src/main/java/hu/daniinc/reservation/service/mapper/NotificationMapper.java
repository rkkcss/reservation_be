package hu.daniinc.reservation.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.domain.Notification;
import hu.daniinc.reservation.service.dto.NotificationDTO;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public abstract class NotificationMapper implements EntityMapper<NotificationDTO, Notification> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    @Mapping(target = "data", source = "data", qualifiedByName = "jsonStringToMap")
    public abstract NotificationDTO toDto(Notification entity);

    @Override
    @Mapping(target = "data", source = "data", qualifiedByName = "mapToJsonString")
    public abstract Notification toEntity(NotificationDTO dto);

    @Override
    @Mapping(target = "data", source = "data", qualifiedByName = "mapToJsonString")
    public abstract void partialUpdate(@MappingTarget Notification entity, NotificationDTO dto);

    @Named("jsonStringToMap")
    protected Map<String, Object> jsonStringToMap(String dataString) {
        if (dataString == null) return null;
        try {
            return objectMapper.readValue(dataString, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize notification data", e);
        }
    }

    @Named("mapToJsonString")
    protected String mapToJsonString(Object data) {
        if (data == null) return null;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize notification data", e);
        }
    }
}
