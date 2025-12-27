package com.gpuflight.gpuflbackend.repository;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KernelEventRepository extends CrudRepository<KernelEventEntity, Long> {
}
