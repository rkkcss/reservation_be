package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.service.SseEmitterService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseEmitterServiceImpl implements SseEmitterService {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter addEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    @Override
    public void sendToUser(Long userId, Object data) {
        List<SseEmitter> userEmitters = emitters.getOrDefault(userId, List.of());
        userEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                removeEmitter(userId, emitter);
            }
        });
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
        }
    }

    // Heartbeat
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        Iterator<Map.Entry<Long, List<SseEmitter>>> iterator = emitters.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Long, List<SseEmitter>> entry = iterator.next();
            List<SseEmitter> userEmitters = entry.getValue();
            List<SseEmitter> dead = new ArrayList<>();

            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                } catch (Exception e) {
                    dead.add(emitter);
                }
            }

            userEmitters.removeAll(dead);

            if (userEmitters.isEmpty()) {
                iterator.remove();
            }
        }
    }
}
