package com.gpuflight.gpuflbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.service.EventProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventProcessingService eventProcessingService;

    public EventController(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @PostMapping("/{eventType}")
    public ResponseEntity<String> receiveEvent(@PathVariable String eventType, @RequestBody String json) {
        try {
            MetricType type = MetricType.valueOf(eventType.toLowerCase());
            eventProcessingService.processEvent(type, json);
            return ResponseEntity.ok("Event received and processed as " + type);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid event type: " + e.getMessage());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Failed to parse event: " + e.getMessage());
        }
    }
}
