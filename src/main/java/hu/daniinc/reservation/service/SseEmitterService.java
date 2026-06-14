package hu.daniinc.reservation.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
    SseEmitter addEmitter(Long userId);
    void sendToUser(Long userId, Object data);
}
