package com.gpuflight.gpuflbackend.controller;


import com.gpuflight.gpuflbackend.model.presentation.HostSummaryDto;
import com.gpuflight.gpuflbackend.model.presentation.InitEventDto;
import com.gpuflight.gpuflbackend.model.presentation.ProfileSampleDto;
import com.gpuflight.gpuflbackend.model.presentation.SystemEventDto;
import com.gpuflight.gpuflbackend.service.HostService;
import com.gpuflight.gpuflbackend.service.InitEventService;
import com.gpuflight.gpuflbackend.service.ProfileSampleService;
import com.gpuflight.gpuflbackend.service.RetentionService;
import com.gpuflight.gpuflbackend.service.SystemEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final InitEventService initEventService;
    private final SystemEventService systemEventService;
    private final HostService hostService;
    private final ProfileSampleService profileSampleService;
    private final RetentionService retentionService;

    @GetMapping("/hosts")
    public ResponseEntity<List<HostSummaryDto>> getHosts() {
        log.debug("Getting host summaries");
        return ResponseEntity.ok(hostService.getHostSummaries());
    }

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

    @GetMapping("/profile-samples")
    public ResponseEntity<List<ProfileSampleDto>> getProfileSamples(
            @RequestParam String sessionId) {
        return ResponseEntity.ok(profileSampleService.getBySessionId(sessionId));
    }

    @PostMapping("/admin/retention/run")
    public ResponseEntity<Map<String, Integer>> triggerRetention() {
        int deleted = retentionService.runCleanup();
        return ResponseEntity.ok(Map.of("deletedSessions", deleted));
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
