package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.ScopeEventDao;
import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.ScopeBeginEvent;
import com.gpuflight.gpuflbackend.model.ScopeEndEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Service
@Slf4j
public class ScopeEventServiceImpl implements ScopeEventService {
    private final ScopeEventDao scopeEventDao;
    private final ObjectMapper objectMapper;

    public ScopeEventServiceImpl(ScopeEventDao scopeEventDao, @Qualifier("ingestionObjectMapper") ObjectMapper objectMapper) {
        this.scopeEventDao = scopeEventDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addScopeEventBegin(EventWrapper eventWrapper) {
        try {
            ScopeBeginEvent event = objectMapper.readValue(eventWrapper.data(), ScopeBeginEvent.class);
            Instant eventTime = epochToInstant(event.tsNs());
            scopeEventDao.saveScopeEvent(ScopeEventEntity.builder()
                    .tsNs(event.tsNs())
                    .time(eventTime)
                    .sessionId(event.sessionId())
                    .name(event.name())
                    .tag(event.tag())
                    .scopeDepth(event.scopeDepth())
                    .userScope(event.userScope())
                    .build());
        } catch (Exception e) {
            log.error("Failed to process ScopeBeginEvent. Data: {}", eventWrapper.data(), e);
            throw new RuntimeException("Error processing ScopeBeginEvent", e);
        }
    }

    @Override
    public void addScopeEventEnd(EventWrapper eventWrapper) {
        try {
            ScopeEndEvent event = objectMapper.readValue(eventWrapper.data(), ScopeEndEvent.class);
            scopeEventDao.updateScopeEventEnd(ScopeEventEntity.builder()
                    .tsNs(event.tsNs())
                    .userScope(event.userScope())
                    .scopeDepth(event.scopeDepth())
                    .sessionId(event.sessionId())
                    .name(event.name())
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to process ScopeEndEvent. Data: {}", eventWrapper.data(), e);
            throw new RuntimeException("Error processing ScopeEndEvent", e);
        }
    }
}
