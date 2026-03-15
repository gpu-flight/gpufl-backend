package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import com.gpuflight.gpuflbackend.model.presentation.CudaStaticDeviceDto;

import java.util.Collection;
import java.util.List;

public interface CudaDeviceService {
    List<CudaStaticDeviceDto> getCudaStaticDevices(String sessionId);
    List<CudaStaticDeviceEntity> getCudaStaticDeviceEntities(Collection<String> sessionIds);
}
