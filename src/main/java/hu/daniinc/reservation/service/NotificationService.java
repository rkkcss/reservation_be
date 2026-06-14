package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.service.dto.NotificationDTO;
import hu.daniinc.reservation.service.dto.NotificationEventDTO;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void handle(NotificationEventDTO event);

    Page<NotificationDTO> getNotifications(Long businessEmployeeId, Boolean unReadOnly, Pageable pageable);

    void markAsRead(Long id, Long businessEmployeeId, User user);

    long getUnreadCount(Long businessEmployeeId, User user);

    Map<Long, Long> getUnreadCountsByBusiness(User user);
}
