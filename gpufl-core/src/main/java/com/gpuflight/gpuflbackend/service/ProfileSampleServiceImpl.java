package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.ProfileSampleDao;
import com.gpuflight.gpuflbackend.dao.ScopeEventDao;
import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;
import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.input.ProfileSampleEvent;
import com.gpuflight.gpuflbackend.model.presentation.ProfileSampleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProfileSampleServiceImpl implements ProfileSampleService {
    private final ObjectMapper objectMapper;
    private final ProfileSampleDao profileSampleDao;
    private final ScopeEventDao scopeEventDao;

    public ProfileSampleServiceImpl(@Qualifier("ingestionObjectMapper") ObjectMapper objectMapper,
                                    ProfileSampleDao profileSampleDao,
                                    ScopeEventDao scopeEventDao) {
        this.objectMapper = objectMapper;
        this.profileSampleDao = profileSampleDao;
        this.scopeEventDao = scopeEventDao;
    }

    @Override
    public void addProfileSample(EventWrapper eventWrapper) {
        try {
            ProfileSampleEvent event = objectMapper.readValue(eventWrapper.data(), ProfileSampleEvent.class);

            // Resolve scope name from the most recently closed scope before this event's timestamp.
            // CUPTI delivers SASS metric samples as a batch after the profiling range ends, so
            // timestamp-based lookup is the reliable way to associate them with a scope.
            String scopeName = null;
            ScopeEventEntity scope = scopeEventDao.findLatestCompletedBefore(event.sessionId(), event.tsNs());
            if (scope != null) {
                scopeName = scope.getName();
            }

            // Map the two SASS metric names to their respective columns.
            // Each PC offset produces two events (one per metric); only smsp__sass_inst_executed
            // increments occurrence_count so each scope run is counted exactly once per instruction.
            boolean isSassInst   = "smsp__sass_inst_executed".equals(event.metricName());
            boolean isSassThread = "smsp__sass_thread_inst_executed".equals(event.metricName());

            long instExecuted        = isSassInst   ? (event.metricValue() != null ? event.metricValue() : 0L) : 0L;
            long threadInstExecuted  = isSassThread ? (event.metricValue() != null ? event.metricValue() : 0L) : 0L;
            long sampleCount         = event.sampleCount() != null ? event.sampleCount().longValue() : 0L;

            ProfileSampleEntity entity = ProfileSampleEntity.builder()
                    .sessionId(event.sessionId())
                    .scopeName(scopeName)
                    .sampleKind(event.sampleKind())
                    .pcOffset(event.pcOffset())
                    .functionName(event.functionName())
                    .sourceFile(event.sourceFile())
                    .sourceLine(event.sourceLine())
                    .instExecuted(instExecuted)
                    .threadInstExecuted(threadInstExecuted)
                    .stallReason(event.stallReason())
                    .reasonName(event.reasonName())
                    .sampleCount(sampleCount)
                    .occurrenceCount(1)
                    .build();

            // smsp__sass_thread_inst_executed is the paired metric for an already-counted instruction;
            // use merge() to accumulate the value without double-counting occurrence_count.
            if (isSassThread) {
                profileSampleDao.merge(entity);
            } else {
                profileSampleDao.save(entity);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse profile_sample event. Data: {}", eventWrapper.data(), e);
        }
    }

    @Override
    public List<ProfileSampleDto> getBySessionId(String sessionId) {
        return profileSampleDao.findBySessionId(sessionId).stream()
                .map(e -> new ProfileSampleDto(
                        e.getId(),
                        e.getSessionId(),
                        e.getScopeName(),
                        e.getSampleKind(),
                        e.getFunctionName(),
                        e.getPcOffset(),
                        e.getSourceFile(),
                        e.getSourceLine(),
                        e.getInstExecuted(),
                        e.getThreadInstExecuted(),
                        e.getStallReason(),
                        e.getReasonName(),
                        e.getSampleCount(),
                        e.getOccurrenceCount(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}
