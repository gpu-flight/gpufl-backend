package com.gpuflight.gpuflbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.InitEvent;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.presentation.InitEventDto;
import com.gpuflight.gpuflbackend.service.EventProcessingService;
import com.gpuflight.gpuflbackend.service.InitEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
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
    private final InitEventService initEventService;

    private final ObjectMapper objectMapper;
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
        log.debug("Received event of type {}: {}", eventType, json);
        EventWrapper event = objectMapper.readValue(json, EventWrapper.class);
        MetricType type = MetricType.valueOf(eventType.toLowerCase());
        eventProcessingService.processEvent(type, event);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event processed successfully");
        response.put("eventType", type.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/init")
    public ResponseEntity<List<InitEventDto>> getInitEvent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo) {
        
        if (dateTo == null) {
            dateTo = Instant.now();
        }
        if (dateFrom == null) {
            dateFrom = dateTo.minus(24, ChronoUnit.HOURS);
        }

        log.debug("Getting init events from {} to {}", dateFrom, dateTo);
        List<InitEventDto> events = initEventService.getInitEvents(dateFrom, dateTo);
        return ResponseEntity.ok(events);
    }
}
