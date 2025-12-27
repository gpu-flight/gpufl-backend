package com.gpuflight.gpuflbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gpuflight.gpuflbackend.model.MetricEvent;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.service.EventProcessingService;
import com.gpuflight.gpuflbackend.service.JsonEventParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final JsonEventParser jsonEventParser;
    private final EventProcessingService eventProcessingService;

    public EventController(JsonEventParser jsonEventParser, EventProcessingService eventProcessingService) {
        this.jsonEventParser = jsonEventParser;
        this.eventProcessingService = eventProcessingService;
    }

    @PostMapping("/{eventType}")
    public ResponseEntity<String> receiveEvent(@PathVariable String eventType, @RequestBody String json) {
        try {
            MetricType type = MetricType.valueOf(eventType);
            MetricEvent event = jsonEventParser.parseEvent(json, type);
            eventProcessingService.processEvent(event);
            return ResponseEntity.ok("Event received and processed as " + event.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid event type or failed to parse: " + e.getMessage());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Failed to parse event: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> receiveEvent(@RequestBody String json) {
        try {
            MetricEvent event = jsonEventParser.parseEvent(json);
            eventProcessingService.processEvent(event);
            return ResponseEntity.ok("Event received and processed as " + event.getClass().getSimpleName());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Failed to parse event: " + e.getMessage());
        }
    }
}
