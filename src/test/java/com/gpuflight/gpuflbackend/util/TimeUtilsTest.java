package com.gpuflight.gpuflbackend.util;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilsTest {

    @Test
    void testEpochToInstant_Zero() {
        long epochNs = 0L;
        Instant expected = Instant.EPOCH;
        assertEquals(expected, TimeUtils.epochToInstant(epochNs));
    }

    @Test
    void testEpochToInstant_OneSecond() {
        long epochNs = 1_000_000_000L;
        Instant expected = Instant.ofEpochSecond(1);
        assertEquals(expected, TimeUtils.epochToInstant(epochNs));
    }

    @Test
    void testEpochToInstant_WithNanos() {
        long epochNs = 1_000_000_500L;
        Instant expected = Instant.ofEpochSecond(1, 500);
        assertEquals(expected, TimeUtils.epochToInstant(epochNs));
    }

    @Test
    void testEpochToInstant_TypicalValue() {
        // 2023-10-27T10:00:00Z = 1698393600 seconds
        long seconds = 1698393600L;
        long nanos = 123456789L;
        long epochNs = seconds * 1_000_000_000L + nanos;

        Instant expected = Instant.ofEpochSecond(seconds, nanos);
        assertEquals(expected, TimeUtils.epochToInstant(epochNs));
    }

    @Test
    void testEpochToInstant_specificValue() {
        long epochNs = 1766907920508090978L;
        Instant expected = TimeUtils.epochToInstant(epochNs);
        System.out.println(expected);
    }
}
