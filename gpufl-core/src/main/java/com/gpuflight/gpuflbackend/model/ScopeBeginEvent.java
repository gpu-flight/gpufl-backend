package com.gpuflight.gpuflbackend.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ScopeBeginEvent(
        int pid,
        String app,
        String sessionId,
        String name,
        String tag,
        long tsNs,
        HostSample host,
        List<DeviceSample> devices,
        String userScope,
        int scopeDepth
) {}
