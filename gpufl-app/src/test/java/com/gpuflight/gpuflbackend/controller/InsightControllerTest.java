package com.gpuflight.gpuflbackend.controller;

import com.gpuflight.gpuflbackend.model.InsightDto;
import com.gpuflight.gpuflbackend.model.SessionInsightsDto;
import com.gpuflight.gpuflbackend.service.InsightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InsightControllerTest {

    @Mock
    private InsightService insightService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new InsightController(insightService)).build();
    }

    @Test
    void getInsights_returnsOkWithEmptyList() throws Exception {
        String sessionId = "session-1";
        when(insightService.getInsights(sessionId)).thenReturn(
                SessionInsightsDto.builder().sessionId(sessionId).insights(Collections.emptyList()).build()
        );

        mockMvc.perform(get("/api/v1/insights/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.insights").isArray());

        verify(insightService).getInsights(sessionId);
    }

    @Test
    void getInsights_returnsInsights() throws Exception {
        String sessionId = "session-2";
        InsightDto insight = InsightDto.builder()
                .severity("HIGH")
                .category("DIVERGENCE")
                .functionName("myKernel")
                .title("Warp divergence detected")
                .message("25% thread efficiency")
                .metric("threadEfficiency=25.0%")
                .build();
        when(insightService.getInsights(sessionId)).thenReturn(
                SessionInsightsDto.builder().sessionId(sessionId).insights(List.of(insight)).build()
        );

        mockMvc.perform(get("/api/v1/insights/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insights[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.insights[0].category").value("DIVERGENCE"))
                .andExpect(jsonPath("$.insights[0].title").value("Warp divergence detected"));

        verify(insightService).getInsights(sessionId);
    }
}
