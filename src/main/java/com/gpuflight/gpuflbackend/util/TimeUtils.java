package com.gpuflight.gpuflbackend.util;

import java.time.Instant;

public class TimeUtils {
    private static final long SECONDS_TO_NANOS = 1_000_000_000L;


    public static Instant epochToInstant(long epochNs) {
        long sec = epochNs / SECONDS_TO_NANOS;
        long nanos = epochNs % SECONDS_TO_NANOS;
        return Instant.ofEpochSecond(sec, nanos);
    }
}
