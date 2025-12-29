package com.gpuflight.gpuflbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.service.EventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for receiving and processing monitoring events.
 * All exceptions are handled by GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventProcessingService eventProcessingService;

    /**
     * Receives and processes monitoring events.
     *
     * @param eventType The type of event (init, kernel_start, etc.)
     * @param json The event data in JSON format
     * @return Success response with event details
     * @throws JsonProcessingException if JSON parsing fails
     * @throws IllegalArgumentException if event type is invalid
     */
    @PostMapping("/{eventType}")
    public ResponseEntity<Map<String, Object>> receiveEvent(
            @PathVariable String eventType,
            @RequestBody String json) throws JsonProcessingException {

        MetricType type = MetricType.valueOf(eventType.toLowerCase());
        log.debug("Received event of type: {}", type);

        eventProcessingService.processEvent(type, json);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event processed successfully");
        response.put("eventType", type.toString());

        return ResponseEntity.ok(response);
    }
}
