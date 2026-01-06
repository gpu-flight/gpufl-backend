package com.gpuflight.gpuflbackend.controller;


import com.gpuflight.gpuflbackend.model.presentation.InitEventDto;
import com.gpuflight.gpuflbackend.model.presentation.SystemEventDto;
import com.gpuflight.gpuflbackend.service.InitEventService;
import com.gpuflight.gpuflbackend.service.SystemEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final InitEventService initEventService;
    private final SystemEventService systemEventService;

    @GetMapping("/init")
    public ResponseEntity<List<InitEventDto>> getInitEvent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo) {

        Instant[] range = calculateRange(dateFrom, dateTo);
        log.debug("Getting init events from {} to {}", range[0], range[1]);
        List<InitEventDto> events = initEventService.getInitEvents(range[0], range[1]);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/system")
    public ResponseEntity<List<SystemEventDto>> getSystemEvents(
            @RequestParam String sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo) {

        Instant[] range = calculateRange(dateFrom, dateTo);
        log.debug("Getting system events for session {} from {} to {}", sessionId, range[0], range[1]);
        List<SystemEventDto> events = systemEventService.getSystemEvents(sessionId, range[0], range[1]);
        return ResponseEntity.ok(events);
    }

    private Instant[] calculateRange(Instant dateFrom, Instant dateTo) {
        if (dateTo == null) {
            dateTo = Instant.now();
        }
        if (dateFrom == null) {
            dateFrom = dateTo.minus(24, ChronoUnit.HOURS);
        }
        return new Instant[]{dateFrom, dateTo};
    }
}
