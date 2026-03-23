package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.ProfileSampleDao;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.presentation.ProfileSampleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProfileSampleServiceImpl implements ProfileSampleService {
    private final ProfileSampleDao profileSampleDao;

    public ProfileSampleServiceImpl(ProfileSampleDao profileSampleDao) {
        this.profileSampleDao = profileSampleDao;
    }

    @Override
    public void addProfileSample(EventWrapper eventWrapper) {
        // Profile samples are now ingested via profile_sample_batch messages handled by BatchIngestionService.
        log.warn("addProfileSample called on legacy path — use BatchIngestionService for profile_sample_batch");
    }

    @Override
    public List<ProfileSampleDto> getBySessionId(String sessionId) {
        return profileSampleDao.findBySessionId(sessionId).stream()
                .map(e -> new ProfileSampleDto(
                        e.getId(),
                        e.getSessionId(),
                        e.getScopeName(),
                        e.getDeviceId(),
                        e.getSampleKind(),
                        e.getFunctionName(),
                        e.getPcOffset(),
                        e.getMetricName(),
                        e.getMetricValue(),
                        e.getStallReason(),
                        e.getOccurrenceCount(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}
