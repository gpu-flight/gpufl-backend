package com.gpuflight.gpuflbackend.model;

public record EventWrapper(
        String data,
        long agentSendingTime,
        String hostname,
        String ipAddr
) {}
