package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.service.NotificationService;
import hu.daniinc.reservation.service.SseEmitterService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.NotificationDTO;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api")
public class SseResource {

    private final SseEmitterService sseEmitterService;
    private final UserService userService;
    private final NotificationService notificationService;

    public SseResource(SseEmitterService sseEmitterService, UserService userService, NotificationService notificationService) {
        this.sseEmitterService = sseEmitterService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return sseEmitterService.addEmitter(user.getId());
    }

    @GetMapping("/notifications/{businessEmployeeId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
        @PathVariable Long businessEmployeeId,
        @RequestParam(required = false) Boolean unreadOnly,
        Pageable pageable
    ) {
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Page<NotificationDTO> page = notificationService.getNotifications(businessEmployeeId, unreadOnly, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        headers.add("X-Unread-Count", String.valueOf(notificationService.getUnreadCount(businessEmployeeId, user)));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam Long businessEmployeeId) {
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        notificationService.markAsRead(id, businessEmployeeId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications/unread-counts")
    public ResponseEntity<Map<Long, Long>> getUnreadCounts() {
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ResponseEntity.ok(notificationService.getUnreadCountsByBusiness(user));
    }
}
