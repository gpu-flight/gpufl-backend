package com.gpuflight.gpuflbackend.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileSampleEvent(
        int pid,
        String app,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("ts_ns") long tsNs,
        @JsonProperty("device_id") int deviceId,
        @JsonProperty("corr_id") long corrId,
        @JsonProperty("sample_kind") String sampleKind,
        // sass_metric fields
        @JsonProperty("metric_name") String metricName,
        @JsonProperty("metric_value") Long metricValue,
        @JsonProperty("pc_offset") String pcOffset,
        // shared
        @JsonProperty("function_name") String functionName,
        @JsonProperty("source_file") String sourceFile,
        @JsonProperty("source_line") Integer sourceLine,
        // pc_sampling fields
        @JsonProperty("sample_count") Integer sampleCount,
        @JsonProperty("stall_reason") Integer stallReason,
        @JsonProperty("reason_name") String reasonName
) {}
