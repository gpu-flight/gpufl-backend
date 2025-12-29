package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;

public interface CudaDeviceDao {
    void saveCudaDevice(CudaStaticDeviceEntity entity);
}
