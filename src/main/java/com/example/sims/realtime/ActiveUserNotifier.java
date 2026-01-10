package com.example.sims.realtime;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class ActiveUserNotifier {
    private static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public static SseEmitter register() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        return emitter;
    }

    public static void notifyChange() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("active-users").data("update"));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
