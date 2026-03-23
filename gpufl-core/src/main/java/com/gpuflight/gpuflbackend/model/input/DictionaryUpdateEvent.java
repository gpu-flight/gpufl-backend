package com.gpuflight.gpuflbackend.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record DictionaryUpdateEvent(
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("kernel_dict") Map<String, String> kernelDict,
    @JsonProperty("scope_name_dict") Map<String, String> scopeNameDict,
    @JsonProperty("function_dict") Map<String, String> functionDict,
    @JsonProperty("metric_dict") Map<String, String> metricDict
) {}
