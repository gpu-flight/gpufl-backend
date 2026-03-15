package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.CudaDeviceDao;
import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CudaDeviceServiceImplTest {

    @Mock private CudaDeviceDao cudaDeviceDao;

    private CudaDeviceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CudaDeviceServiceImpl(cudaDeviceDao);
    }

    @Test
    void getCudaStaticDevices_noDevices_returnsEmptyList() {
        when(cudaDeviceDao.findBySessionId("session-1")).thenReturn(Collections.emptyList());

        List<?> result = service.getCudaStaticDevices("session-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void getCudaStaticDevices_withDevices_returnsMappedDtos() {
        CudaStaticDeviceEntity entity = CudaStaticDeviceEntity.builder()
                .sessionId("session-1")
                .deviceId(0)
                .name("GPU0")
                .uuid("uuid-0")
                .computeMajor("8")
                .computeMinor("6")
                .l2CacheSizeBytes(4_194_304L)
                .sharedMemPerBlockBytes(49_152L)
                .regsPerBlock(65_536)
                .multiProcessorCount(20)
                .warpSize(32)
                .build();

        when(cudaDeviceDao.findBySessionId("session-1")).thenReturn(List.of(entity));

        List<?> result = service.getCudaStaticDevices("session-1");

        assertEquals(1, result.size());
    }

    @Test
    void getCudaStaticDeviceEntities_delegatesToDao() {
        Set<String> sessionIds = Set.of("s1", "s2");
        when(cudaDeviceDao.findBySessionIds(sessionIds)).thenReturn(Collections.emptyList());

        List<?> result = service.getCudaStaticDeviceEntities(sessionIds);

        assertTrue(result.isEmpty());
        verify(cudaDeviceDao).findBySessionIds(sessionIds);
    }
}
