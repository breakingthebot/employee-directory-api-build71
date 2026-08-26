/*
 * events/EmployeeEventPublisher.java
 * Component managing SSE connection emitters and broadcasting mutation events to subscribed clients.
 * Connects to: dto/EmployeeEventDTO.java, services/impl/EmployeeServiceImpl.java, controllers/EmployeeController.java
 * Created: 2026-08-08
 */
package com.employee.directory.events;

import com.employee.directory.dto.EmployeeEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event publisher maintaining active SseEmitter subscriptions and streaming real-time directory changes.
 */
@Component
public class EmployeeEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeEventPublisher.class);
    private static final Long DEFAULT_TIMEOUT = 1800000L; // 30 minutes

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registers a new SseEmitter subscription connection.
     * 
     * @return Initialized SseEmitter instance.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.add(emitter);
        logger.info("New SSE client subscribed. Active emitters: {}", emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.info("SSE emitter completed. Remaining emitters: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            logger.info("SSE emitter timed out. Remaining emitters: {}", emitters.size());
        });

        emitter.onError((ex) -> {
            emitters.remove(emitter);
            logger.error("SSE emitter error: {}. Remaining emitters: {}", ex.getMessage(), emitters.size());
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to Employee Directory Real-Time SSE Stream"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Broadcasts an event payload to all active SSE subscribers.
     * 
     * @param event Event payload to stream.
     */
    public void publishEvent(EmployeeEventDTO event) {
        List<SseEmitter> failedEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.getEventType().name())
                        .data(event));
            } catch (Exception e) {
                logger.warn("Failed to send SSE event to subscriber: {}", e.getMessage());
                failedEmitters.add(emitter);
            }
        }

        if (!failedEmitters.isEmpty()) {
            emitters.removeAll(failedEmitters);
            logger.info("Removed {} disconnected SSE emitters.", failedEmitters.size());
        }
    }

    /**
     * Returns the current count of active SSE client subscribers.
     * 
     * @return Number of connected emitters.
     */
    public int getActiveSubscriberCount() {
        return emitters.size();
    }
}
