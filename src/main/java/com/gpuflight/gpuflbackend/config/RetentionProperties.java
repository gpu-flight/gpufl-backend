package com.gpuflight.gpuflbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "gpufl.retention")
public class RetentionProperties {
    private boolean enabled = true;
    private int defaultDays = 30;
    private String cleanupCron = "0 0 2 * * *";
}
