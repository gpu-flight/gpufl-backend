package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;

public interface KernelEventService {
    void addKernelBeginEvent(EventWrapper eventWrapper);
    void addKernelEndEvent(EventWrapper eventWrapper);
}
