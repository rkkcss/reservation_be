package hu.daniinc.reservation.service.dto;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import java.util.Map;

public class NotificationEventDTO {

    private BusinessEmployeeDTO businessEmployee;
    private String type;
    private Map<String, Object> data;

    public NotificationEventDTO(BusinessEmployeeDTO businessEmployee, String type, Map<String, Object> data) {
        this.businessEmployee = businessEmployee;
        this.type = type;
        this.data = data;
    }

    public BusinessEmployeeDTO getBusinessEmployee() {
        return businessEmployee;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
