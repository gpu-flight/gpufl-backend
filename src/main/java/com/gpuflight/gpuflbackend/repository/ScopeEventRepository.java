package com.gpuflight.gpuflbackend.repository;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScopeEventRepository extends CrudRepository<ScopeEventEntity, Long> {
}
