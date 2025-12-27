package com.gpuflight.gpuflbackend.repository;

import com.gpuflight.gpuflbackend.entity.DeviceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<DeviceEntity, String> {
}
