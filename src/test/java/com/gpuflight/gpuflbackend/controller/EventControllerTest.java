package com.gpuflight.gpuflbackend.controller;

import com.gpuflight.gpuflbackend.model.presentation.InitEventDto;
import com.gpuflight.gpuflbackend.model.presentation.SystemEventDto;
import com.gpuflight.gpuflbackend.service.HostService;
import com.gpuflight.gpuflbackend.service.InitEventService;
import com.gpuflight.gpuflbackend.service.ProfileSampleService;
import com.gpuflight.gpuflbackend.service.RetentionService;
import com.gpuflight.gpuflbackend.service.SystemEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock private InitEventService initEventService;
    @Mock private SystemEventService systemEventService;
    @Mock private HostService hostService;
    @Mock private ProfileSampleService profileSampleService;
    @Mock private RetentionService retentionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EventController controller = new EventController(initEventService, systemEventService, hostService, profileSampleService, retentionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getInitEvent_noParams_usesDefaultRange_returns200() throws Exception {
        when(initEventService.getInitEvents(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/events/init"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(initEventService).getInitEvents(any(Instant.class), any(Instant.class));
    }

    @Test
    void getInitEvent_withDateRange_passesParamsToService() throws Exception {
        when(initEventService.getInitEvents(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/events/init")
                        .param("dateFrom", "2025-01-01T00:00:00Z")
                        .param("dateTo",   "2025-01-02T00:00:00Z"))
                .andExpect(status().isOk());

        verify(initEventService).getInitEvents(
                eq(Instant.parse("2025-01-01T00:00:00Z")),
                eq(Instant.parse("2025-01-02T00:00:00Z")));
    }

    @Test
    void getInitEvent_withResults_returnsJson() throws Exception {
        InitEventDto dto = new InitEventDto(
                100, "demo", "session-1", "demo.log",
                Instant.EPOCH, 1_000_000_000L, null, 10,
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                null, null
        );
        when(initEventService.getInitEvents(any(), any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/events/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value("session-1"));
    }

    @Test
    void getSystemEvents_noParams_usesDefaultRange_returns200() throws Exception {
        when(systemEventService.getSystemEvents(eq("session-1"), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/events/system").param("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getSystemEvents_withDateRange_passesParamsToService() throws Exception {
        when(systemEventService.getSystemEvents(any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/events/system")
                        .param("sessionId", "session-1")
                        .param("dateFrom", "2025-01-01T00:00:00Z")
                        .param("dateTo",   "2025-01-02T00:00:00Z"))
                .andExpect(status().isOk());

        verify(systemEventService).getSystemEvents(
                eq("session-1"),
                eq(Instant.parse("2025-01-01T00:00:00Z")),
                eq(Instant.parse("2025-01-02T00:00:00Z")));
    }
}
