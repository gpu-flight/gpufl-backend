package com.gpuflight.gpuflbackend.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProfileSampleBatch(
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("batch_id") long batchId,
    @JsonProperty("base_time_ns") long baseTimeNs,
    List<String> columns,
    List<List<Number>> rows
) {}
