package com.gpuflight.gpuflbackend.model;

public enum MetricType {
    init,
    kernel_event,   // unified record — replaces the split kernel_start/kernel_end pair
    kernel_start,
    kernel_end,
    scope_begin,
    scope_end,
    shutdown,
    system_start,
    system_stop,
    system_sample
}
