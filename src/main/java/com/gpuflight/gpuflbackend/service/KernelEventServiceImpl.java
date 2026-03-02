package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.KernelEventDao;
import com.gpuflight.gpuflbackend.mapper.KernelEventMapper;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.input.KernelBeginEvent;
import com.gpuflight.gpuflbackend.model.input.KernelEndEvent;
import com.gpuflight.gpuflbackend.model.input.KernelEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

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

    @Override
    public void addKernelBeginEvent(EventWrapper eventWrapper) {
        try {
            KernelBeginEvent event = objectMapper.readValue(eventWrapper.data(), KernelBeginEvent.class);
            kernelEventDao.saveKernelBegin(KernelEventMapper.mapToKernelEventEntityFromBegin(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse kernel begin event. Data: {}", eventWrapper.data(), e);
        }
    }

    @Override
    public void addKernelEndEvent(EventWrapper eventWrapper) {
        try {
            KernelEndEvent event = objectMapper.readValue(eventWrapper.data(), KernelEndEvent.class);
            kernelEventDao.updateKernelEnd(KernelEventMapper.mapToKernelEventEntityFromEnd(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse kernel end event. Data: {}", eventWrapper.data(), e);
        }
    }
}
