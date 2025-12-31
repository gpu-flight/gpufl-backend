package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;

public interface ScopeEventService {
    void addScopeEventBegin(EventWrapper eventWrapper);
    void addScopeEventEnd(EventWrapper eventWrapper);
}
