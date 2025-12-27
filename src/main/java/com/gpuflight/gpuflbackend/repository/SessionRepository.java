package com.gpuflight.gpuflbackend.repository;

import com.gpuflight.gpuflbackend.entity.SessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<SessionEntity, String> {
}
