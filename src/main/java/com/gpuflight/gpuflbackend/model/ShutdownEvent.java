package com.gpuflight.gpuflbackend.model;

public record ShutdownEvent(
        int pid,
        String app,
        String sessionId,
        long tsNs
){}
