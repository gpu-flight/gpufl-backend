package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.DeviceMetricDao;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceMetricServiceImplTest {

    @Mock private DeviceMetricDao deviceMetricDao;

    private DeviceMetricServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeviceMetricServiceImpl(deviceMetricDao);
    }

    @Test
    void saveDeviceMetric_legacyMethod_doesNotCallDao() {
        DeviceSample sample = new DeviceSample(
                0, "GPU0", "uuid-0", "NVIDIA", 1,
                200L, 7800L, 8000L,
                80, 30, 75, 120_000,
                1800, 1800, 9000,
                0, 0, 2048L, 1024L
        );

        // Device metrics are now ingested via batch messages; legacy method is a no-op.
        service.saveDeviceMetric(sample, "system_start", "session-1", Instant.EPOCH, 1_000_000_000L);

        verifyNoInteractions(deviceMetricDao);
    }
}
