package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.CudaDeviceDao;
import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import com.gpuflight.gpuflbackend.mapper.CudaStaticDeviceMapper;
import com.gpuflight.gpuflbackend.model.presentation.CudaStaticDeviceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CudaDeviceServiceImpl {
    private final CudaDeviceDao cudaDeviceDao;

    public List<CudaStaticDeviceDto> getCudaStaticDevices(String sessionId) {
        List<CudaStaticDeviceEntity> entities = cudaDeviceDao.findBySessionId(sessionId);

        return entities.stream()
                .map(CudaStaticDeviceMapper::mapToCudaStaticDeviceDto)
                .collect(Collectors.toList());
    }

    public List<CudaStaticDeviceEntity> getCudaStaticDeviceEntities(Collection<String> sessionIds) {
        return cudaDeviceDao.findBySessionIds(sessionIds);
    }

}
