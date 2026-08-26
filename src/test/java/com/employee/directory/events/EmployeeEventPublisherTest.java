/*
 * events/EmployeeEventPublisherTest.java
 * Unit tests for EmployeeEventPublisher verifying SSE subscription registration and broadcasting.
 * Connects to: events/EmployeeEventPublisher.java, dto/EmployeeEventDTO.java
 * Created: 2026-08-08
 */
package com.employee.directory.events;

import com.employee.directory.dto.EmployeeEventDTO;
import com.employee.directory.enums.EmployeeEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeEventPublisherTest {

    @Test
    @DisplayName("subscribe - Registers new SseEmitter and increments active subscriber count")
    void subscribe_RegistersEmitter() {
        EmployeeEventPublisher publisher = new EmployeeEventPublisher();
        assertEquals(0, publisher.getActiveSubscriberCount());

        SseEmitter emitter = publisher.subscribe();

        assertNotNull(emitter);
        assertEquals(1, publisher.getActiveSubscriberCount());
    }

    @Test
    @DisplayName("publishEvent - Broadcasts event to active subscribers")
    void publishEvent_BroadcastsToSubscribers() {
        EmployeeEventPublisher publisher = new EmployeeEventPublisher();
        publisher.subscribe();

        EmployeeEventDTO event = new EmployeeEventDTO(
                EmployeeEventType.EMPLOYEE_CREATED,
                1L,
                "New employee created",
                null
        );

        assertDoesNotThrow(() -> publisher.publishEvent(event));
    }
}
