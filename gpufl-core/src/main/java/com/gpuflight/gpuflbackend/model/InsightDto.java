package com.gpuflight.gpuflbackend.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InsightDto {
    String severity;
    String category;
    String kernelName;
    String functionName;
    String title;
    String message;
    String metric;
}
