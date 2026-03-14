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

            // For SASS metrics (corrId=0), associate to the most recently closed scope
            // because CUPTI delivers SASS metric samples as a batch after the profiling range ends
            String scopeId = null;
            if (event.corrId() == 0) {
                ScopeEventEntity scope = scopeEventDao.findLatestCompletedBefore(event.sessionId(), event.tsNs());
                if (scope != null) {
                    scopeId = scope.getId();
                }
            }

            ProfileSampleEntity entity = ProfileSampleEntity.builder()
                    .sessionId(event.sessionId())
                    .scopeId(scopeId)
                    .tsNs(event.tsNs())
                    .deviceId(event.deviceId())
                    .corrId(event.corrId())
                    .sampleKind(event.sampleKind())
                    .metricName(event.metricName())
                    .metricValue(event.metricValue())
                    .pcOffset(event.pcOffset())
                    .functionName(event.functionName())
                    .sourceFile(event.sourceFile())
                    .sourceLine(event.sourceLine())
                    .sampleCount(event.sampleCount())
                    .stallReason(event.stallReason())
                    .reasonName(event.reasonName())
                    .build();
            profileSampleDao.save(entity);
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
                        e.getScopeId(),
                        e.getTsNs(),
                        e.getDeviceId(),
                        e.getCorrId(),
                        e.getSampleKind(),
                        e.getMetricName(),
                        e.getMetricValue(),
                        e.getPcOffset(),
                        e.getFunctionName(),
                        e.getSourceFile(),
                        e.getSourceLine(),
                        e.getSampleCount(),
                        e.getStallReason(),
                        e.getReasonName(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}
