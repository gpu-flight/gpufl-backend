package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.presentation.ProfileSampleDto;

import java.util.List;

public interface ProfileSampleService {
    void addProfileSample(EventWrapper eventWrapper);
    List<ProfileSampleDto> getBySessionId(String sessionId);
}
