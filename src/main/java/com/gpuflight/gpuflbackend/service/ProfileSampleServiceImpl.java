package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.ProfileSampleDao;
import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;
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

    public ProfileSampleServiceImpl(@Qualifier("ingestionObjectMapper") ObjectMapper objectMapper,
                                    ProfileSampleDao profileSampleDao) {
        this.objectMapper = objectMapper;
        this.profileSampleDao = profileSampleDao;
    }

    @Override
    public void addProfileSample(EventWrapper eventWrapper) {
        try {
            ProfileSampleEvent event = objectMapper.readValue(eventWrapper.data(), ProfileSampleEvent.class);
            ProfileSampleEntity entity = ProfileSampleEntity.builder()
                    .sessionId(event.sessionId())
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
