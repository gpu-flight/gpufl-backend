package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.KernelEventDao;
import com.gpuflight.gpuflbackend.mapper.KernelEventMapper;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.input.KernelEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KernelEventServiceImpl implements KernelEventService {
    private final ObjectMapper objectMapper;
    private final KernelEventDao kernelEventDao;

    public KernelEventServiceImpl(@Qualifier("ingestionObjectMapper") ObjectMapper objectMapper, KernelEventDao kernelEventDao) {
        this.objectMapper = objectMapper;
        this.kernelEventDao = kernelEventDao;
    }

    @Override
    public void addKernelEvent(EventWrapper eventWrapper) {
        try {
            KernelEvent event = objectMapper.readValue(eventWrapper.data(), KernelEvent.class);
            kernelEventDao.saveKernelEvent(KernelEventMapper.mapToKernelEventEntityFromEvent(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse kernel event. Data: {}", eventWrapper.data(), e);
        }
    }
}
