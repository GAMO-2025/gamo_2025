package gamo.web.letter.service;

import gamo.web.letter.dto.LetterSseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.IOException;

@Component
public class LetterSseEmitters {

    // 한 멤버에 여러 emitter 가능하도록 List 사용
    private final ConcurrentMap<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // 구독 추가
    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(memberId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(memberId, emitter));
        emitter.onTimeout(() -> removeEmitter(memberId, emitter));
        emitter.onError(e -> removeEmitter(memberId, emitter));

        return emitter;
    }

    // 특정 멤버에게 이벤트 발송
    public void sendLetterUpdate(Long memberId, LetterSseDTO dto) {
        List<SseEmitter> memberEmitters = emitters.get(memberId);
        if (memberEmitters != null) {
            memberEmitters.forEach(emitter -> {
                try {
                    emitter.send(dto);
                } catch (IOException e) {
                    removeEmitter(memberId, emitter);
                }
            });
        }
    }

    // emitter 제거
    private void removeEmitter(Long memberId, SseEmitter emitter) {
        List<SseEmitter> memberEmitters = emitters.get(memberId);
        if (memberEmitters != null) {
            memberEmitters.remove(emitter);
            if (memberEmitters.isEmpty()) {
                emitters.remove(memberId);
            }
        }
    }
}