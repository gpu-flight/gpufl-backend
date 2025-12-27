package com.gpuflight.gpuflbackend.repository;

import com.gpuflight.gpuflbackend.entity.SystemMetricEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMetricRepository extends CrudRepository<SystemMetricEntity, Long> {
}
