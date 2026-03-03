package com.gpuflight.gpuflbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.exception.GlobalExceptionHandler;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.service.EventProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventIngestionControllerTest {

    @Mock private EventProcessingService eventProcessingService;

    private MockMvc mockMvc;

    private static final String EVENT_WRAPPER_JSON = """
            {
              "data": "{\\"pid\\":1}",
              "agentSendingTime": 0,
              "hostname": "localhost",
              "ipAddr": "127.0.0.1"
            }
            """;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        EventIngestionController controller = new EventIngestionController(eventProcessingService, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void receiveEvent_validType_returns200WithSuccessBody() throws Exception {
        mockMvc.perform(post("/api/v1/events/kernel_event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_WRAPPER_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.eventType").value("kernel_event"));

        verify(eventProcessingService).processEvent(eq(MetricType.kernel_event), any());
    }

    @Test
    void receiveEvent_initType_routes() throws Exception {
        mockMvc.perform(post("/api/v1/events/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_WRAPPER_JSON))
                .andExpect(status().isOk());

        verify(eventProcessingService).processEvent(eq(MetricType.init), any());
    }

    @Test
    void receiveEvent_unknownType_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/events/unknown_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENT_WRAPPER_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void receiveEvent_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/events/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }
}
