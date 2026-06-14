package hu.daniinc.reservation.service.impl;

import static hu.daniinc.reservation.service.specifications.NotificationSpecification.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.Notification;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.repository.NotificationRepository;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.NotificationService;
import hu.daniinc.reservation.service.SseEmitterService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.NotificationDTO;
import hu.daniinc.reservation.service.dto.NotificationEventDTO;
import hu.daniinc.reservation.service.mapper.BusinessEmployeeMapper;
import hu.daniinc.reservation.service.mapper.NotificationMapper;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;
    private final NotificationMapper notificationMapper;
    private final UserService userService;
    private final BusinessEmployeeMapper businessEmployeeMapper;
    private final BusinessEmployeeService businessEmployeeService;

    public NotificationServiceImpl(
        NotificationRepository notificationRepository,
        SseEmitterService sseEmitterService,
        ObjectMapper objectMapper,
        NotificationMapper notificationMapper,
        UserService userService,
        BusinessEmployeeMapper businessEmployeeMapper,
        BusinessEmployeeService businessEmployeeService
    ) {
        this.notificationRepository = notificationRepository;
        this.sseEmitterService = sseEmitterService;
        this.objectMapper = objectMapper;
        this.notificationMapper = notificationMapper;
        this.userService = userService;
        this.businessEmployeeMapper = businessEmployeeMapper;
        this.businessEmployeeService = businessEmployeeService;
    }

    @Async
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationEventDTO event) {
        try {
            // save to DB
            Notification notification = new Notification();
            notification.setBusinessEmployee(businessEmployeeMapper.toEntity(event.getBusinessEmployee()));
            notification.setType(event.getType());
            notification.setData(objectMapper.writeValueAsString(event.getData()));
            notification.setRead(false);
            notificationRepository.save(notification);

            // send to client
            sseEmitterService.sendToUser(
                event.getBusinessEmployee().getUser().getId(),
                Map.of(
                    "id",
                    notification.getId(),
                    "type",
                    event.getType(),
                    "data",
                    event.getData(),
                    "read",
                    false,
                    "createdAt",
                    notification.getCreatedAt(),
                    "businessEmployeeId",
                    notification.getBusinessEmployee().getId()
                )
            );
        } catch (Exception e) {
            LOG.error("Notification handling failed", e);
        }
    }

    @Override
    public Page<NotificationDTO> getNotifications(Long businessEmployeeId, Boolean unreadOnly, Pageable pageable) {
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new RuntimeException("User not found"));
        BusinessEmployee businessEmployee = businessEmployeeMapper.toEntity(businessEmployeeService.findById(businessEmployeeId));

        if (!businessEmployee.getUser().getId().equals(user.getId())) {
            throw new GeneralException("Access denied", "access-denied", HttpStatus.FORBIDDEN);
        }

        Specification<Notification> spec = Specification.where(businessEmployeeEquals(businessEmployee.getId())).and(
            Boolean.TRUE.equals(unreadOnly) ? isUnread() : null
        );

        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return notificationRepository.findAll(spec, sortedPageable).map(notificationMapper::toDto);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long businessEmployeeId, User user) {
        notificationRepository
            .findByIdAndBusinessEmployeeAndUser(notificationId, businessEmployeeId, user)
            .orElseThrow(() ->
                new GeneralException("Notification not found or access denied", "notification-not-found", HttpStatus.NOT_FOUND)
            )
            .setRead(true);
    }

    @Override
    public long getUnreadCount(Long businessEmployeeId, User user) {
        return notificationRepository.countUnreadByBusinessEmployeeAndUser(businessEmployeeId, user);
    }

    @Override
    public Map<Long, Long> getUnreadCountsByBusiness(User user) {
        return notificationRepository
            .countUnreadGroupedByBusinessEmployee(user)
            .stream()
            .collect(
                Collectors.toMap(
                    row -> (Long) row[0], // businessEmployee id
                    row -> (Long) row[1] // unread count
                )
            );
    }
}
