package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;

public interface KernelEventService {
    void addKernelEvent(EventWrapper eventWrapper);
}
